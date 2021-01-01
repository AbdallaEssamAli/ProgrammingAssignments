package DiningPhilosophers;

public class DiningServerImpl implements DiningServer {

    // different philosopher states
    enum State {
        THINKING, HUNGRY, EATING
    };

    // number of philosophers
    public static final int NUM_OF_PHILS = 5;

    // array to record each philosopher's state
    private State[] state;

    public Condition[] self;

    public Lock lock = null;

    public DiningServerImpl() {
        lock = new ReentrantLock();
        state = new State[NUM_OF_PHILS];
        self = new Condition[NUM_OF_PHILS];
        for (int i = 0; i < NUM_OF_PHILS; i++) {
            state[i] = State.THINKING;
            self[i] = lock.newCondition();
        }
    }

    // called by a philosopher when they wish to eat 
    @Override
    public void takeForks(int pnum) {
        try {
            lock.lock();
            state[pnum] = State.HUNGRY;
            if ((state[(pnum - 1 + NUM_OF_PHILS) % NUM_OF_PHILS] != State.EATING)
                    && (state[(pnum + 1) % NUM_OF_PHILS] != State.EATING)) {
                state[pnum] = State.EATING;
            } else {
                try {
                    self[pnum].await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DiningServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                state[pnum] = State.EATING;
            }
        } finally {
            lock.unlock();
        }
    }

    // called by a philosopher when they are finished eating 
    @Override
    public void returnForks(int pnum) {
        lock.lock();
        try {
            state[pnum] = State.THINKING;

            int left = (pnum - 1 + NUM_OF_PHILS) % NUM_OF_PHILS;
            int left2 = (pnum - 2 + NUM_OF_PHILS) % NUM_OF_PHILS;
            if ((state[left] == State.HUNGRY)
                    && (state[left2] != State.EATING)) {
                self[left].signal();
            }

            if ((state[(pnum + 1) % NUM_OF_PHILS] == State.HUNGRY)
                    && (state[(pnum + 2) % NUM_OF_PHILS] != State.EATING)) {
                self[(pnum + 1) % NUM_OF_PHILS].signal();
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void test(int i) {
        if (state[(i + 1) % 5] != State.EATING
                && state[(i + 4) % 5] != State.EATING) {
            state[i] = State.EATING;
        }
    }

    public int leftNeighbor(int i) {
        int left = (i + NUM_OF_PHILS) % NUM_OF_PHILS;
        
        if (state[left] == State.EATING) {
            --i;
        }
        return i;
    }

    public int rightNeighbor(int i) {
        int right = (i + NUM_OF_PHILS) % NUM_OF_PHILS;
        if (state[right] == State.EATING) {
            return ++i;
        }
        return i;
    }
}