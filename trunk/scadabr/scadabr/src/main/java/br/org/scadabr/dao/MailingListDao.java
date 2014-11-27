/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.web.dwr.beans.RecipientListEntryBean;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

/**
 *
 * @author aploese
 */
public interface MailingListDao {

    public MailingList getMailingList(int referenceId);

    public Set<String> getRecipientAddresses(List<RecipientListEntryBean> activeRecipients, DateTime dateTime);
    
}
