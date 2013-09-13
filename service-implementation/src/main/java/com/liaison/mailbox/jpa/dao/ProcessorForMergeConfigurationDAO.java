package com.liaison.mailbox.jpa.dao;

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.ProcessorForMerge;
@NamedQueries({
    @NamedQuery(name=ProcessorForMergeConfigurationDAO.FIND_PROCESSOR_BY_MAILBOX_PROFILE_LINKERID,
    	query=	"SELECT procsr FROM ProcessorForMerge procsr"+
		    	" inner join procsr.mailboxSchedProfile mbx_profile"+
		    	" inner join mbx_profile.mailbox mbx"+
		    	" inner join mbx_profile.scheduleProfilesRef sch_prof"+
		    	" where mbx.pguid = :" + ProcessorForMergeConfigurationDAO.MB_PGUID +
		    	" and sch_prof.pguid = :"+ ProcessorForMergeConfigurationDAO.PROFILE_PGUID +
		    	" order by procsr.procsrExecutionOrder"
    	)
})

public interface ProcessorForMergeConfigurationDAO extends GenericDAO <ProcessorForMerge> {
	
	public static final String FIND_PROCESSOR_BY_MAILBOX_PROFILE_LINKERID = "findProcsrBymailboxProfileLinkerId";
	public static final String MB_PGUID = "mailbox_pguid";
	public static final String PROFILE_PGUID = "profile_pguid";
	
	public List<ProcessorForMerge> find(String mailboxId, String schedulerProfileId);
}

