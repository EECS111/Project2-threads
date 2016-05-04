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
		lock = new Lock();
		speakerWaitingQ = new Condition(lock);
		speakerSending = new Condition(lock);
		listenerWaitingQ = new Condition(lock);
		listenerReceiving = new Condition(lock);
		
		listenerWaiting = speakerWaiting = received = false;
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
		
		lock.acquire();
		
		//if speaker is not waiting, add to waiting queue
		while(speakerWaiting){
			speakerWaitingQ.sleep();
		}
		
		speakerWaiting = true;
		hold = word;
		
		//if a listener is not waiting or a messaged is not received
		//wake up a listener and put the speaker to a sending queue
		while (!listenerWaiting || !received){
			listenerReceiving.wake();
			speakerSending.sleep();
		}
		
		//set to false so listener can get into a receiving queue
		// and speaker to sending queue
		listenerWaiting = speakerWaiting = received = false;
		
		//wakes waiting speaker
		speakerWaitingQ.wake();
		listenerWaitingQ.wake();
		lock.release();
			
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		
		lock.acquire();
		while(listenerWaiting){
			listenerWaitingQ.sleep();
		}
		
		listenerWaiting = true;
		//cannot continue unless listener Waiting is false. set by speak
		
		//if no speaker is waiting
		//set receiver to be ready to receive message
		while( !speakerWaiting ){
			listenerReceiving.sleep();
		}
		
		//passes while loop if there is a speaker sending a message
		// and wakes speaker in sendingQueue
		
		speakerSending.wake();
		received = true;
		lock.release();
		return hold;
		
	}
	
	private Condition speakerWaitingQ; //queue for waiting speakers
	private Condition speakerSending; //speaker sending word
	private Condition listenerWaitingQ; //queue for waiting listeners
	private Condition listenerReceiving; //listener recieving word
	
	private boolean listenerWaiting; //true if listener is waiting
	private boolean speakerWaiting; //true if speaker is waiting
	private boolean received; //true if message is recieved
	
	private int hold; //the word
	private Lock lock;
	
	
	
}
