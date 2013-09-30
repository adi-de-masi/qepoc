package ch.upc.ctsp.qepoc.server;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Slightly changed from oracle's example on server sockets.
 * 
 * @author ademasi
 * 
 */
public class Protocol {

  public final static String[] VALID_INPUTS    = { "/modem/00AB123456/ip", "/scopes/10.30.22.12", "/scopes/mbsXZY001-01-pc/cmts",
      "/cmts/mbsXZY001/modem/00AB123456/cmId", "/cmts/mbsXZY001/ip", "/snmp/62.2.11.22/1.3.6.2.1.2.3.4.203342" };
  public final static String[] ANSWERS         = { "10.30.22.12", "mbsXZY001-01-pc", "mbsXZY001", "203342", "62.2.11.22", "L1/0/1/1" };
  public static final String   WELCOME_MESSAGE = "Welcome, young padavan. Send me your request!";

  public String processInput(final String theInput) {
    String theOutput = null;
    if (theInput == null) {
      // initializing
      theOutput = WELCOME_MESSAGE;
    } else {
      theOutput = findAnswer(theInput);
    }
    return theOutput;
  }

  private String findAnswer(final String theInput) {
    if (theInput.equals("Bye.") || theInput.equals("Shut.")) {
      return "";
    }
    String theOutput = "Wrong is the Request. Mind what you have learned. Save you it can.";
    for (int i = 0; i < VALID_INPUTS.length; i++) {
      if (VALID_INPUTS[i].compareTo(theInput) == 0) {
        theOutput = ANSWERS[i];
        break;
      }
    }
    return theOutput;
  }
}
