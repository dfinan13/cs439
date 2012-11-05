package cpu;
/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

import simulator.CPUBase;
import simulator.Debug;
import simulator.ICPU;
import simulator.IOS;
import simulator.IPageTableEntry;
import simulator.ITLBEntry;
import simulator.Simulator;
import simulator.SystemInfo;

public class CPU extends CPUBase { 

    private IOS os;
    private SystemInfo m_sysinfo;
    
    /**
     * Constructor used by Simulator for each CPU
     * @param id  This CPU's ID
     * @param os  The IOS reference
     * @param info The SystemInfo
     */
    public CPU(final int id, final IOS os, SystemInfo info) {
        super(id);	// Leave this as the first statement
        this.os = os;
        this.m_sysinfo = info;
    }
    
    /**
     * Called by CPUBase to convert a (vaddr, frame_number)
     * to a physical address.
     * Does not require lookup, just shifting/masking based
     * on the SystemInfo.
     */
    @Override
    public long vaddr2paddr(final long vaddr, final int fnum) {
        throw new IllegalStateException("Implement vaddr2paddr()");

    }

   
    /**
     * Called by CPUBase.
     * If the virtual address is mapped, returns the frame number
     * of the frame that "holds" the page.
     * Otherwise, returns -1.
     * NOTE!!!!!!!!!!!!!!!!!!!!!!! TO PRODUCE THE CORRECT OUTPUT!!!
     * When xlate() fails to find a valid mapping with a resident page,
     * xlate() must call:  logXlateFault(page_number).
     * Otherwise, xlate() must call: logXlateHit(page_number, frame_number). 
     * Note that on a "hit", xlate() must call IOS.setFrameReferencedTime()
     * to ensure it knows how to track LRU.
     */
    public int xlate(final long virtual_address) {
    	int frame_number = 0;
    	int page_number = 0;
    	
    	String hex = Simulator.hex(virtual_address);
    	
    	IPageTableEntry[][] ptbr = this.getPTBR();
    	
    	//Do conversion
    	long offset_bits = m_sysinfo.getPageSizeBytesLog2();
    	long VPN_bits = m_sysinfo.getPageCountLog2();
    	long VPN1_bits = m_sysinfo.getPageTableLevel1EntryCountLog2();
    	long VPN2_bits = VPN_bits - VPN1_bits;
    	long p2_mask = (1 << VPN2_bits) - 1;
    	long p2 = (virtual_address >>> offset_bits) & p2_mask;
    	
    	//for now set long p1 = 1;
    	long p1 = 0;
    	//Get size of offset
    	
    	hex = Simulator.hex(p2);
    	System.out.println("P2 (hex): " + hex + "P1(base 10): " + p2);
    	 
    	IPageTableEntry entry = ptbr[(int)p1][(int)p2];
    	frame_number = entry.getFrameNumber();
    	//End conversion
    	
    	if (frame_number == -1) {
    		logXlateFault(page_number);
    	} else {
    		logXlateHit(page_number, frame_number);
    		int time = Simulator.getTime();
    		os.setFrameReferencedTime(frame_number, time);
    	}
    	
    	return frame_number;
    }


}
