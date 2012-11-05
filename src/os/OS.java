package os;
/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

import simulator.Debug;
import simulator.ICPU;
import simulator.IPageTableEntry;
import simulator.IProcess;
import simulator.IProcessState;
import simulator.ITLBEntry;
import simulator.Simulator;
import simulator.SystemInfo;


public class OS extends simulator.OSBase {
    final SystemInfo m_sysinfo;    

    public OS(SystemInfo si) {
    	super(si);
        m_sysinfo = si;        
    }

    /*
     * Use class PageTableEntry to represent the state of each
     * page of a process's address space.
     * The CPU class expects some class of OS to implement IPageTableEntry
     * Use this if you like.
     * But however you do it, it must implement IPageTableEntry!
     */
    private static class PageTableEntry implements IPageTableEntry {
 
    	private long index; //either p1 or p2 dependent on level
    	private long address; //either to page table or physical frame
    	private boolean present;
    	private int token;

    	public PageTableEntry(int index, int address)
    	{
    		//Store the page table level
    		this.address = address;
    		this.present = false;
    		this.index = index;
    	}
    	
    	public void setPresent(boolean valid) {
    		this.present = valid;
    	}
    	
    	public void setToken(int token) {
    		this.token = token;
    	}
    	
    	public int getToken() {
    		return this.token;
    	}
    	
        @Override
        public boolean isResident() {
            return present;
        }
        
        public void setFrameNumber(long frame_number) {
        	this.address = frame_number;
        }

        @Override
        public int getFrameNumber() {
        		return (int)this.address;
        }

        
    }

    
    /*
     * Use class AddressSpace for managing a process's 
     * "Address Space",consisting mainly of its page table.
     * Keep in mind the following:
       -- When the OS allocates an address space, none of its containing
          pages are considered "allocated".
          A process must system calls (see syscallAlloc()) to
          indicate to the OS that the virtual memory range is now
          "allocated".
              This accomplishes two goals:
                  The OS doesn't need to maintain data structures for
                     the max number of pages, and
                  Programs generating bad addresses (program bugs) will
                     result in a segmentation fault.
       -- For this project, the Simulator will call syscallAlloc() for
          the "text" (instruction) pages of the process before it's dispatched 
          (normally done by the OS loader).
     */
    private class AddressSpace {
        PageTableEntry[][] m_page_table;
    	private int p1_count;
    	private int p2_count;
    	
        //Address Space Constructor
        public AddressSpace() {
        	//Page Count
        	int page_count = m_sysinfo.getPageCountLog2();
        	int p1_size = m_sysinfo.getPageTableLevel1EntryCountLog2();
        	int p2_size = page_count - p1_size;
        	
        	this.p1_count = (int)Math.pow(2, p1_size);
        	this.p2_count = (int)Math.pow(2, p2_size);
        	//Allocate space in the page tables
        	//for p1 count entries in level 1
        	//and p2 count entries in level 2
        	this.m_page_table = new PageTableEntry[this.p1_count][this.p2_count];
        	//Initialize the page table with unallocated values
        	for(int i = 0; i < this.p1_count; i++) {
        		for(int j = 0; j < this.p2_count; j++) {
	        		OS.PageTableEntry entry = new OS.PageTableEntry(i,-1);
	        		this.m_page_table[i][j] = entry ;
        		}
        	}
        }
        
        public PageTableEntry[][] getPageTable() {
        	return this.m_page_table;
        }
        
        public int getLevel1Count() {
        	return this.p1_count;
        }
        
        public int getLevel2Count() {
        	return this.p2_count;
        }
    }
 
    
    /*
     * You will need a class to represent the state of each Frame of memory.
     * Likewise, some other class will need to contain/manage the 
     * collection of these frames.
     * You may wish to use an inner class to manage the collection of frames.
     * Then, add an inner class with an instance per frame of memory.
     * Here's one way to structure this.
     * Feel free to delete this code if you'd rather do it some other way.
     */
    private class FrameInfo {
    	
    	private int frame_size;
    	private int frame_count;
    	
    	Frame[] frame_list;
    	
    	public FrameInfo() {
    		this.frame_size = (int) Math.pow(2, m_sysinfo.getPageSizeBytesLog2());
    		this.frame_count = m_sysinfo.getFrameCountLog2();
    	}
    	
        class Frame {
        	private int frame_number;
        	
        	public Frame() {
        		
        	}
        	
        	public void freeFrame() {
        		
        	}
        }
    }


   /**
    * Indicates that the specified range of (virtual memory) pages should now be
    * considered as "allocated".
    * (Only allocated pages can be referenced - instruction fetching
    *  and data loads/stores).
    *  
    * Call Sequence:
    * 	CPU->instr.execute() on a Syscall_Alloc instruction.
    *    instr.execute() -> Simulator.syscallAlloc() -> OS.syscallAlloc()
    * @param p
    * @param start_page_number
    * @param number_of_pages
    * @return indicator of success (true), failure (false)
    */
    @Override
    public boolean syscallAlloc(IProcess p, int start_page_number, int number_of_pages) {
        //Get the address space for p
    	AddressSpace addr_space = (AddressSpace) p.getAddressSpace();
    	
    	int page_count = m_sysinfo.getPageCountLog2();
    	int p1_size = m_sysinfo.getPageTableLevel1EntryCountLog2();
    	int p2_size = page_count - p1_size;  
    	
    	//Check if we have more than one entry in Level 1 Page Table
    	int p1_count = addr_space.getLevel1Count();
    	//Check to see if number_of_pages > than this value
    	//instantly return false
    	int p2_count = addr_space.getLevel2Count();
  	
        //Handle the case where p1_count > 1;
    	
    	
    	//Assumption: treating start_page_number as p2
		if(number_of_pages > p2_count) return false;
		//Get the pagetables for current process
		PageTableEntry[][] m_page_table = addr_space.getPageTable();

		
		PageTableEntry PTE;
		for (int i = start_page_number; i < number_of_pages; i++) {
			//Set each valid bit in the PTE to valid to make it allocated
				PTE = m_page_table[0][i];
				//check if already allocated via isResident()
				if(PTE.isResident()) {
					return false;
				} else {
					PTE.setPresent(true);
				}
		}
    		

    	return true;
    }
    
    /**
     * Called by Simulator when a process begins.
     * Perform any setup required to allow the process to
     * run.
     * This should include creating an "Address Space" for
     * the process, which can be saved in the process object
     * using IProcess.setAddressSpace(Object), and retrieved
     * using getAddressSpace().
     * @param p
     */
    @Override
    public void initProcess(IProcess p) {
        // Create an address space for p
        AddressSpace a = new AddressSpace();
        p.setAddressSpace(a);

    }

    /**
     * Called when an instruction fetch (at ip_vaddr)
     * results in a page fault (not in tlb or page-table).
     *
     * If the vaddr is not valid (hasn't been allocated or
     *   beyond the processes address space size), return false.
     * If the vaddr is not an instruction page, return false.
     * (see IProcess.getTextByteCount())
     * 
     * Otherwise, allocate a frame of memory to back the page,
     * update p's page table to reflect the new mapping, 
     * and return true.
     * @param cpu
     * @param p
     * @param ip_virtual
     */
    @Override
    public boolean pageFaultInstruction(ICPU cpu, IProcess p, long ip_virtual) {
        throw new IllegalStateException("Implement pageFaultInstruction");
    }

    /**
     * Called when an insruction's attempt to load/store
     * to memory at virutal address d_vaddr
     * results in a page fault (not in tlb or page-table).
     *
     * If the vaddr is not valid (hasn't been allocated or
     *   beyond the processes address space size), return false.
     * Otherwise, allocate a frame of memory to back the page,
     * update p's page table to reflect the new mapping, 
     * and return true.
     * @param cpu
     * @param p
     * @param ip_virtual
     */
    @Override
    public boolean pageFaultData(ICPU cpu, IProcess p, long d_vaddr) {
        throw new IllegalStateException("Implement pageFaultData");
    }
    
    /**
     * Call sequence:  CPU -> Simulator.schedule() -> OS.schedule()
     * You'll need to call sched.schedule() (note: included below)
     * If a process is found, then restore its state before returning.
     * @param cpu
     * @return
     */
    @Override
    public IProcess schedule(ICPU cpu) {
        IProcess p = m_sysinfo.getScheduler().schedule(cpu);
        if (p != null) { //process is found
        	 //Restore state before returning
        	IProcessState state = p.getState();
        	long m_ip = state.getIP();
        	cpu.setIP(m_ip);
        	AddressSpace temp = (AddressSpace) p.getAddressSpace();
        	PageTableEntry[][] page_table = temp.getPageTable();
        	cpu.setPTBR(page_table);
        }
        return p;
    }

    /**
     * Called by Simulator whenever the currently running proccess
     * is exiting.
     * This can be the result of a "Syscall_Exit" instruction, or
     *   because of a non-recoverable fault (e.g. invalid instruction,
     *   invalid virtual address, etc.)
     *  
     * @param cpu
     * @param p
     */
    @Override
    public void processExiting(ICPU cpu, IProcess p) {
        // TODO. The currently executing process is exiting.
    	//  Release any allocated frames back to the free-frame pool.
    	//  Be sure to do this in ascending virtual address order
    	//    adding any (now) free frames to the end of the free-frame
    	//    list.
		
        super.processExiting(cpu, p);  // Leave this at the end of the method
    }

    /**
     * Call Sequence:  CPU->Simulator.quantumExpired()->OS.quantumExpired()
     * Process p is being undispatched
     * @param p
     * @param cpu
     * @param ticks_given
     */
    @Override
    public void quantumExpired(IProcess p, ICPU cpu, int ticks_given) {
        // TODO.
    	// Be sure to capture the volatile state of p
    	//  since another process is going to get dispatched
    	// Be sure to "walk" through the TLB entries 
    	//  updating your "frame LRU" list based on when
    	//  the frames were referenced.
    	
        
        super.quantumExpired(p, cpu, ticks_given);  // Leave this at the end of the method
    }

    /**
     * Called by CPU on a successful xlate().
     * Must be implemented to correctly manage LRU
     */
    @Override
    public void setFrameReferencedTime(int frame_num, int time) {
        // TODO.
    	// Implement this so that LRU works correctly.
    	
    }
    
}
