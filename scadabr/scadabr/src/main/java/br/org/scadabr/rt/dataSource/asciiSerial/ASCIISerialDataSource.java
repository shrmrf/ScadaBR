package br.org.scadabr.rt.dataSource.asciiSerial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.org.scadabr.vo.dataSource.asciiSerial.ASCIISerialDataSourceVO;
import br.org.scadabr.vo.dataSource.asciiSerial.ASCIISerialPointLocatorVO;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.web.i18n.LocalizableMessage;

public class ASCIISerialDataSource extends PollingDataSource {

    private final static Logger LOG = LoggerFactory.getLogger(ASCIISerialDataSource.class);
	public static final int POINT_READ_EXCEPTION_EVENT = 1;
	public static final int DATA_SOURCE_EXCEPTION_EVENT = 2;
	private final ASCIISerialDataSourceVO<?> vo;
	private Enumeration portList;
	private InputStream inSerialStream;
	private OutputStream outSerialStream;
	private SerialPort sPort;

	public ASCIISerialDataSource(ASCIISerialDataSourceVO<?> vo) {
		super(vo, true);
		this.vo = vo;
		setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(),
				vo.isQuantize());

		portList = CommPortIdentifier.getPortIdentifiers();
		getPort(vo.getCommPortId());
		configurePort(getsPort());

	}

	@Override
	protected void doPoll(long time) {

		try {
			// nao tem dados
			if (getInSerialStream().available() == 0) {

				for (DataPointRT dataPoint : enabledDataPoints) {
					ASCIISerialPointLocatorVO dataPointVO = dataPoint.getVO()
							.getPointLocator();
					if (dataPointVO.getCommand() != null) {
						getOutSerialStream().write(
								dataPointVO.getCommand().getBytes());
					}

					dataPoint.updatePointValue(new PointValueTime(
							"Sem dados disponíveis !", new Date().getTime()));
				}
				raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, time, true,
						new LocalizableMessage("event.exception2",
								vo.getName(), "Sem dados disponíveis !"));
			} else if (getInSerialStream().available() > 0) {
				byte[] readBuffer = new byte[vo.getBufferSize()];
				try {

					while (getInSerialStream().available() > 0) {
						getInSerialStream().read(readBuffer);
					}

					int count = 0;

					for (int i = 0; i < readBuffer.length; i++) {
						if (readBuffer[i] == 0) {
							continue;
						}

						count++;
					}

					byte[] finalSerial = new byte[count];
					System.out.println("Byte: " + finalSerial);

					for (int i = 0; i < finalSerial.length; i++) {
						finalSerial[i] = readBuffer[i];
					}

					String result = new String(finalSerial);
					System.out.println("Result: " + result);

					// Pos processamento
					String posResults[];

					// String onde vai ser aplicada a Regex
					String posResultFinal = "";

					// Debug posResults
					posResults = result.split(vo.getCharX());
					for (int i = 0; i < posResults.length; i++) {
						System.out.println("posResult: " + posResults[i]);
					}

					if (!vo.getCharX().equals(null)) {
						System.out.println("Caracterer :" + vo.getCharX());
						posResults = result.split(vo.getCharX());

						if (posResults.length != 0) {
							posResultFinal = posResults[0];
							System.out.println("posResultFinal: "
									+ posResultFinal);
							System.out.println("tamanho: " + posResults.length);
							for (int i = 0; i < posResults.length; i++) {
								System.out.println(posResults[i]);
							}
						}
					}

					// if (vo.getnChar() != 0) {
					// posResultFinal = result.substring(0, vo.getnChar());
					// System.out.println("IMPRIMIUUUU");
					// }

					for (DataPointRT dataPoint : enabledDataPoints) {
						try {
							ASCIISerialPointLocatorVO dataPointVO = dataPoint
									.getVO().getPointLocator();
							if (dataPointVO.getCommand() != null) {
								getOutSerialStream().write(
										dataPointVO.getCommand().getBytes());
							}

							// troquei posResult por result
							MangoValue value = getValue(dataPointVO,
									posResultFinal);
							long timestamp = time;
							// write to port

							if (dataPointVO.isCustomTimestamp()) {
								try {
									timestamp = getTimestamp(dataPointVO,
											posResultFinal);
								} catch (Exception e) {
									raiseEvent(
											POINT_READ_EXCEPTION_EVENT,
											time,
											true,
											new LocalizableMessage(
													"event.exception2", vo
															.getName(), e
															.getMessage()));
									timestamp = time;
								}

							}

							dataPoint.updatePointValue(new PointValueTime(
									value, timestamp));
						} catch (Exception e) {
							raiseEvent(POINT_READ_EXCEPTION_EVENT, time, true,
									new LocalizableMessage("event.exception2",
											vo.getName(), e.getMessage()));
							e.printStackTrace();
						}

					}

				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
		}
	}

	private MangoValue getValue(ASCIISerialPointLocatorVO point, String arquivo)
			throws Exception {
		String valueRegex = point.getValueRegex();
		Pattern valuePattern = Pattern.compile(valueRegex);
		Matcher matcher = valuePattern.matcher(arquivo);
		MangoValue value = null;
		String strValue = null;
		boolean found = false;
		while (matcher.find()) {
			found = true;
			strValue = matcher.group();
			value = MangoValue.stringToValue(strValue, point.getMangoDataType());
		}
		if (!found) {
			throw new Exception("Value string not found (regex: " + valueRegex
					+ ")");
		}

		return value;
	}

	private long getTimestamp(ASCIISerialPointLocatorVO point, String arquivo)
			throws Exception {
		long timestamp = new Date().getTime();
		String dataFormat = point.getTimestampFormat();
		String tsRegex = point.getTimestampRegex();
		Pattern tsPattern = Pattern.compile(tsRegex);
		Matcher tsMatcher = tsPattern.matcher(arquivo);

		boolean found = false;
		while (tsMatcher.find()) {
			found = true;
			String tsValue = tsMatcher.group();
			timestamp = new SimpleDateFormat(dataFormat).parse(tsValue)
					.getTime();
		}

		if (!found) {
			throw new Exception("Timestamp string not found (regex: " + tsRegex
					+ ")");
		}

		return timestamp;
	}

	@Override
	public void terminate() {
		super.terminate();
		getsPort().close();
	}

	@Override
	public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime,
			SetPointSource source) {

	}

	public void configurePort(SerialPort port) {

		try {
			setInSerialStream(port.getInputStream());
			setOutSerialStream(port.getOutputStream());
		} catch (Exception e) {
		}

		port.notifyOnDataAvailable(true);

		try {
			port.setSerialPortParams(vo.getBaudRate(), vo.getDataBits(),
					vo.getStopBits(), vo.getParity());
		} catch (Exception e) {
		}

	}

	public SerialPort getPort(String port) {
		System.out.println("Tentando abrir a porta: " + port);
		SerialPort serialPort = null;
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList
					.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(port)) {
					try {
						serialPort = (SerialPort) portId.open(port, 10000);
						setsPort(serialPort);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Erro ao abrir a porta !");
						System.out.println("Provavelmente a porta: " + port
								+ " esta em uso !");
					}
				}
			}
		}

		return serialPort;
	}

	public OutputStream getOutSerialStream() {
		return outSerialStream;
	}

	public void setOutSerialStream(OutputStream outSerialStream) {
		this.outSerialStream = outSerialStream;
	}

	public InputStream getInSerialStream() {
		return inSerialStream;
	}

	public void setInSerialStream(InputStream inSerialStream) {
		this.inSerialStream = inSerialStream;
	}

	public SerialPort getsPort() {
		return sPort;
	}

	public void setsPort(SerialPort sPort) {
		this.sPort = sPort;
	}

}
