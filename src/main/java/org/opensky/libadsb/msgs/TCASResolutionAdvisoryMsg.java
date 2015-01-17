package org.opensky.libadsb.msgs;

import java.io.Serializable;
import org.opensky.libadsb.ExtendedSquitter;
import org.opensky.libadsb.exceptions.BadFormatException;

/**
 *  This file is part of org.opensky.libadsb.
 *
 *  org.opensky.libadsb is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  org.opensky.libadsb is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with org.opensky.libadsb.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Decoder for 1090ES TCAS Resolution Advisory Messages.<br>
 * Note: This format only exists in ADS-B versions >= 2 
 * @author Matthias Schäfer <schaefer@sero-systems.de>
 */
public class TCASResolutionAdvisoryMsg extends ExtendedSquitter implements Serializable {
	
	private static final long serialVersionUID = -5356935198993751991L;
	private byte subtype;
	private short active_ra;
	private byte racs_record;
	private boolean ra_terminated;
	private boolean multi_threat_encounter;
	private byte threat_type;
	private int threat_identity;
	
	/**
	 * @param raw_message raw ADS-B aircraft status message as hex string
	 * @throws BadFormatException if message has wrong format
	 */
	public TCASResolutionAdvisoryMsg(String raw_message) throws BadFormatException {
		super(raw_message);
		
		if (this.getFormatTypeCode() != 28)
			throw new BadFormatException("TCAS RA reports must have typecode 28.", raw_message);
		
		byte[] msg = this.getMessage();
		
		subtype = (byte) (msg[0]&0x7);
		if (subtype != 2)
			throw new BadFormatException("TCAS RA reports have subtype 2.", raw_message);
		
		active_ra = (short) (((msg[2]>>>2) | (msg[1]<<6)) & 0x3FFF);
		racs_record = (byte) ((((msg[2]&0x3)<<2) | (msg[3]>>>6)) & 0xF);
		ra_terminated = (msg[3]&0x20) > 0;
		multi_threat_encounter = (msg[3]&0x10) > 0;
		threat_type = (byte) ((msg[3]>>>2)&0x3);
		threat_identity = (msg[6] | (msg[5]<<8) | (msg[4]<<16) | ((msg[4]&0x3)<<24)) & 0x3FFFFFF;
	}
	
	/**
	 * @return the subtype code of the aircraft status report (should always be 2)
	 */
	public byte getSubtype() {
		return subtype;
	}

	/**
	 * @return 14 bits which indicate the characteristics of the resolution advisory
	 *         (Annex 10 V4, 4.3.8.4.2.2.1.1)
	 */
	public short getActiveRA() {
		return active_ra;
	}

	/**
	 * @return 4 bits which indicate all currently active RACs
	 *         (Annex 10 V4, 4.3.8.4.2.2.1.2)
	 */
	public byte getRACRecord() {
		return racs_record;
	}

	/**
	 * @return whether RA previously generated by ACAS has ceased being generated
	 *         (Annex 10 V4, 4.3.8.4.2.2.1.3)
	 */
	public boolean hasRATerminated() {
		return ra_terminated;
	}

	/**
	 * @return whether two or more simultaneous threats are currently being processed
	 *         (Annex 10 V4, 4.3.8.4.2.2.1.4)
	 */
	public boolean hasMultiThreatEncounter() {
		return multi_threat_encounter;
	}

	/**
	 * @return the threat type indicator:
	 *            0) no identity data in TID
	 *            1) TID contains Mode S transponder address
	 *            2) TID contains altitude, range, bearing
	 *            3) not assigned
	 *         (Annex 10 V4, 4.3.8.4.2.2.1.5)
	 */
	public byte getThreatType() {
		return threat_type;
	}

	/**
	 * @return the threat's identity. Check getThreatType() before.
	 *         (Annex 10 V4, 4.3.8.4.2.2.1.6)
	 */
	public int getThreatIdentity() {
		return threat_identity;
	}

	public String toString() {
		String ret = super.toString()+"\n"+
				"TCAS Resolution Advisory:\n";
		ret += "\tActive RAs:\t\t"+getActiveRA()+"\n";
		ret += "\tCurrent active RACs:\t"+getRACRecord()+"\n";
		ret += "\tRA terminated:\t"+hasRATerminated()+"\n";
		ret += "\tMultiple Threats:\t"+hasMultiThreatEncounter()+"\n";
		ret += "\tThreat type:\t"+getThreatType()+"\n";
		ret += "\tThreat identity:\t"+getThreatIdentity();
		
		return ret;
	}
}
