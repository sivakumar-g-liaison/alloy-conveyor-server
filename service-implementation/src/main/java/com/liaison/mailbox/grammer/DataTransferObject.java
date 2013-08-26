/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer;

import com.liaison.commons.jaxb.GrammarObject;

/**
 * DataTransferObject.java
 * 
 * This is a base class for grouping our DTO JAXB objects. It is the mechanism to
 * use polymorphism to define sub-elements of our manifest documents.
 * 
 * http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
 *
 * @author veerasamyn
 */
public abstract class DataTransferObject implements GrammarObject {

}
