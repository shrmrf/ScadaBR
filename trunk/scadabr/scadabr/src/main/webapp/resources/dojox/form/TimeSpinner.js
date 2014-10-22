define([
	"dojo/_base/lang",
	"dojo/_base/event",
	"dijit/form/_Spinner",
	"dojo/keys",
	"dojo/date",
	"dojo/date/locale",
	"dojo/date/stamp",
	"dojo/_base/declare"
], function(lang, event, Spinner, keys, dateUtil, dateLocale, dateStamp, declare){

return declare("dojox.form.TimeSpinner", Spinner,
{
	// summary:
	//		Time Spinner
	// description:
	//		This widget is the same as a normal NumberSpinner, but for the time component of a date object instead

	required: false,

	adjust: function(/*Object*/ val, /*Number*/ delta){
		return dateUtil.add(val, "minute", delta)
	},

	//FIXME should we allow for constraints in this widget?
	isValid: function(){return true;},

	smallDelta: 1,

	largeDelta: 10,

	timeoutChangeRate: 0.50,

	parse: function(time, locale){
		return dateLocale.parse(time, {selector:"time", formatLength:"medium"});
	},

	format: function(time, locale){
		if(lang.isString(time)){ return time; }
		return dateLocale.format(time, {selector:"time", formatLength:"medium"});
	},

	serialize: dateStamp.toISOString,

	value: "12:00 AM",

   _onKeyDown: function(e){
	   // TODO: this code is just copied from NumberSpinner
		if((e.keyCode == keys.HOME || e.keyCode == keys.END) && !(e.ctrlKey || e.altKey || e.metaKey)
		&& typeof this.get('value') != 'undefined' /* gibberish, so HOME and END are default editing keys*/){
			var value = this.constraints[(e.keyCode == keys.HOME ? "min" : "max")];
			if(value){
				this._setValueAttr(value,true);
			}
			// eat home or end key whether we change the value or not
			event.stop(e);
		}
	}
});
});
