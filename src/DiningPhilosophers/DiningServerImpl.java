package DiningPhilosophers;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class DiningServerImpl implements DiningServer{
    // different philosopher states
    enum State {
        THINKING, HUNGRY, EATING
    };
   final Lock lock;
    // number of philosophers
    public static final int NUM_OF_PHILS = 5;

    // array to record each philosopher's state
    private State[] state;
     private final Condition[] cond;

    
    

    public DiningServerImpl() {
       // DiningServerImpl.NUM_OF_PHILS=num;

        lock = new ReentrantLock();
		state = new State[NUM_OF_PHILS ];
        cond = new Condition[NUM_OF_PHILS];
        for(int i = 0; i < NUM_OF_PHILS; i++){
			state[i] = State.THINKING;
            cond[i] = lock.newCondition();
        }
    }

    // called by a philosopher when they wish to eat 
    @Override
    public void takeForks(int pnum) {
        lock.lock();
		try{
			state[pnum] = State.HUNGRY;
			if( ( state[(pnum-1+NUM_OF_PHILS)%NUM_OF_PHILS] != State.EATING ) &&					
			    (state[(pnum+1)%NUM_OF_PHILS] != State.EATING) ){
				System.out.format("Philosopher %d picks up left chopstick\n", pnum+1);
				System.out.format("Philosopher %d picks up right chopstick\n", pnum+1);
				state[pnum] = State.EATING;
            }
            else {	
				try {
					cond[pnum].await();
                    
                    System.out.format("Philosopher %d picks up left chopstick\n", pnum + 1);
                    System.out.format("Philosopher %d picks up right chopstick\n", pnum + 1);
                    state[pnum] = State.EATING;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		}
		finally{
			lock.unlock();
		} 

        }

    // called by a philosopher when they are finished eating 
    @Override
    public void returnForks(int pnum) {
        lock.lock();
		try{
			System.out.format("Philosopher %d puts down right chopstick\n", pnum+1);
			System.out.format("Philosopher %d puts down left chopstick\n", pnum+1);
			state[pnum] = State.THINKING;
			
			// Tell the left neighbor about the possibility to eat.
			int var1 = (pnum - 1 + NUM_OF_PHILS)%NUM_OF_PHILS;
			int var2 = (pnum - 2 + NUM_OF_PHILS)%NUM_OF_PHILS;
			if( (state[var1] == State.HUNGRY) &&
				(state[var2] != State.EATING) ){
				cond[var1].signal();
			}
			// Tell the right neighbor about the possibility to eat
			if( (state[(pnum+1)%NUM_OF_PHILS] == State.HUNGRY) &&
				(state[(pnum+2)%NUM_OF_PHILS] != State.EATING) ){
				cond[(pnum+1)%NUM_OF_PHILS].signal();
			}
		}// end try
		finally {
			lock.unlock();
		}
	}

    }

