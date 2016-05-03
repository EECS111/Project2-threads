package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired oAff at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		ComLock = new Lock();
		sCond = new Condition(ComLock);
		lCond = new Condition(ComLock);
		sCount = 0;
		lCount = 0;
		wordReady = false;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word the integer to transfer.
	 */
	public void speak(int word) {
		ComLock.acquire();
		sCount++;
		while(lCount <= 0 || wordReady){
			sCond.sleep();
		}
		this.word = word;
		wordReady = true;
		lCond.wakeAll();
		sCount--;
		ComLock.release();	
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		ComLock.acquire();
		lCount++;
		while(!wordReady){
			sCond.wakeAll();
			lCond.sleep();
		}
		int W = this.word;
		wordReady = false;
		
		lCount--;
		ComLock.release();
		return W;
		
	}
	
	private Lock ComLock;
	private Condition sCond;
	private long sCount;
	private Condition lCond;
	private long lCount;
	private boolean wordReady;
	private int word;
}
