--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
--
-- drop FSM_related table 
--

/**
* -- Scripts for DBA to DROP tables manually --
*
* DROP TABLE GATEWAY_OWNR.FSM_EVENT CASCADE CONSTRAINTS;
* DROP TABLE GATEWAY_OWNR.FSM_TRANSITION_LIST CASCADE CONSTRAINTS;
* DROP TABLE GATEWAY_OWNR.FSM_TRANSITION_STATE CASCADE CONSTRAINTS;
* DROP TABLE GATEWAY_OWNR.FSM_STATE_VALUE CASCADE CONSTRAINTS;
* DROP TABLE GATEWAY_OWNR.FSM_STATE CASCADE CONSTRAINTS;
 */

DECLARE
    num_rows integer;    
BEGIN    
    SELECT count(*) INTO num_rows FROM ALL_OBJECTS WHERE object_name = 'FSM_EVENT' AND OWNER = 'GATEWAY_OWNR';
    
    IF num_rows = 1 THEN
        EXECUTE IMMEDIATE    
            'DROP TABLE GATEWAY_OWNR.FSM_EVENT CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    num_rows integer;    
BEGIN    
    SELECT count(*) INTO num_rows FROM ALL_OBJECTS WHERE object_name = 'FSM_TRANSITION_LIST' AND OWNER = 'GATEWAY_OWNR';
    
    IF num_rows = 1 THEN
        EXECUTE IMMEDIATE    
            'DROP TABLE GATEWAY_OWNR.FSM_TRANSITION_LIST  CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    num_rows integer;    
BEGIN    
    SELECT count(*) INTO num_rows FROM ALL_OBJECTS WHERE object_name = 'FSM_TRANSITION_STATE' AND OWNER = 'GATEWAY_OWNR';
    
    IF num_rows = 1 THEN
        EXECUTE IMMEDIATE    
            'DROP TABLE GATEWAY_OWNR.FSM_TRANSITION_STATE  CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    num_rows integer;
BEGIN
    SELECT count(*) INTO num_rows FROM ALL_OBJECTS WHERE object_name = 'FSM_STATE_VALUE' AND OWNER = 'GATEWAY_OWNR';

    IF num_rows = 1 THEN
        EXECUTE IMMEDIATE
            'DROP TABLE GATEWAY_OWNR.FSM_STATE_VALUE CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    num_rows integer;
BEGIN
    SELECT count(*) INTO num_rows FROM ALL_OBJECTS WHERE object_name = 'FSM_STATE' AND OWNER = 'GATEWAY_OWNR';

    IF num_rows = 1 THEN
        EXECUTE IMMEDIATE
            'DROP TABLE GATEWAY_OWNR.FSM_STATE CASCADE CONSTRAINTS';
    END IF;
END;
/