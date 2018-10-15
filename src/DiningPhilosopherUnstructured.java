import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosopherUnstructured 
{
	public static void main( String args[] )
	{
		Lock forkList[] = new ReentrantLock[5];
		
		for ( int i = 0; i < 5; i++ )
		{
			forkList[i] = new ReentrantLock( );
		}
		
		for ( int i = 0; i < 5; i++ )
		{
			if ( i != 4 )
			{
				Thread t = new Thread( new PhilosopherUnstructured( forkList[i], forkList[i + 1], i + 1, false ) );
				t.start();
			}
			else
			{
				// Fork to the right of the last philosopher should be the first fork
				Thread t = new Thread( new PhilosopherUnstructured( forkList[i], forkList[0], i + 1, true ) );
				t.start();
			}
		}
		
	}
}

class PhilosopherUnstructured implements Runnable
{
	// Action states
	enum States
	{
		STATE_EATING,
		STATE_THINKING,
		STATE_HUNGRY,
	}
	
	private Lock left;
	private Lock right;
	private String name;
	private boolean bLast;
	
	public PhilosopherUnstructured( Lock left, Lock right, int number, boolean bIsLastPhilosopher )
	{
		this.left = left;
		this.right = right;
		this.name = "Philosopher " + number;
		this.bLast = bIsLastPhilosopher;
	}
	
	public void run()
	{	
		// The last philosopher should have the first fork to his right
		Lock higher = bLast ? left : right;
		Lock lower = bLast ? right : left; 
		
		while( true )
		{
			boolean bGrabbedLower = false, bGrabbedHigher = false;
			
			// Philosopher will always grab the lower-numbered fork first to ensure deadlocks never occur
			System.out.println( name + ": attempt to acquire fork to " + ( bLast ? "right" : "left" ) );
			
			// Keep trying to grab the lower fork until we succeed
			while ( !bGrabbedLower )
			{
				// Try to grab the lower-numbered fork
				bGrabbedLower = lower.tryLock();
				
				if ( bGrabbedLower )
				{
					// We got the fork
					try
					{
						System.out.println( name + ": acquired " + ( bLast ? "right" : "left" ) + " fork" );				
						System.out.println( name + ": attempt to acquire fork to " + ( bLast ? "left" : "right" ) );
						
						// Keep trying to grab the higher fork until we succeed
						while ( !bGrabbedHigher )
						{
							// Try to grab the higher-numbered fork
							bGrabbedHigher = higher.tryLock();
							
							if ( bGrabbedHigher )
							{
								// We got the fork
								try
								{
									System.out.println( name + ": acquired " + ( bLast ? "left" : "right" ) + " fork" );
									Think( States.STATE_EATING );
								}
								finally
								{
									higher.unlock();
								}
							}
							else
							{
								// We didn't get the fork. Think for a while
								System.out.println( name + ": Failed to aquire "  + ( bLast ? "left" : "right" ) + " fork" );		
								Think( States.STATE_HUNGRY );
							}
						}
					}
					finally
					{
						lower.unlock();
					}
				}
				else
				{
					// We didn't get the fork. Think for a while
					System.out.println( name + ": Failed to aquire "  + ( bLast ? "right" : "left" ) + " fork" );		
					Think( States.STATE_HUNGRY );
				}
			
			}
			
			// Finished eating. Think for a while
			Think( States.STATE_THINKING );
		}
	}
	
	public void Think( States state )
	{
		Random rand = new Random();
		int seconds = 0;
		
		try
		{			
			// Different actions depending on the state
			switch( state )
			{
			case STATE_EATING:
				seconds = rand.nextInt( 9 ) + 1;
				System.out.println( name + ": Eating for " + seconds + " seconds" );
				break;
			case STATE_THINKING:
				seconds = rand.nextInt( 4 ) + 1;
				System.out.println( name + ": Thinking for " + seconds + " seconds" );
				break;
			case STATE_HUNGRY:
				seconds = rand.nextInt( 4 ) + 1;
				System.out.println( name + ": Thinks about how hungry he is for " + seconds + " seconds" );
				break;
			}
			
			Thread.sleep( 1000 * seconds );
		} 
		catch ( InterruptedException e ) 
		{
			e.printStackTrace();
		}
	}
}