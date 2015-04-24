package cloudsim.ext.datacenter;

import java.util.ArrayList;
import java.util.Random;

public class PSOVmLoadBalancer extends VmLoadBalancer {
	
	DatacenterController dcbLocal;
	private DatacenterController dcb;
	public PSOVmLoadBalancer(DatacenterController datacenterController) {
		super();
		dcbLocal = dcb;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getNextAvailableVm() {
		// TODO Auto-generated method stub
		return 0;
	}
	private static final int TARGET = 50;
    private static final int MAX_INPUTS = 3;
    private static final int MAX_PARTICLES = 20;
    private static final int V_MAX = 10;             // Maximum velocity change allowed.

    private static final int MAX_EPOCHS = 200;
    // The particles will be initialized with data randomly chosen within the range
    // of these starting min and max values: 
    private static final int START_RANGE_MIN = 140;
    private static final int START_RANGE_MAX = 190;

    private static ArrayList<Particle> particles = new ArrayList<Particle>();
    
    private static void initialize()
    {
        for(int i = 0; i < MAX_PARTICLES; i++)
        {
            Particle newParticle = new Particle();
            int total = 0;
            for(int j = 0; j < MAX_INPUTS; j++)
            {
                newParticle.data(j, getRandomNumber(START_RANGE_MIN, START_RANGE_MAX));
                total += newParticle.data(j);
            } // j
            newParticle.pBest(total);
            particles.add(newParticle);
        } // i
        return;
    }
    
    private static void PSOAlgorithm()
    {
        int gBest = 0;
        int gBestTest = 0;
        Particle aParticle = null;
        int epoch = 0;
        boolean done = false;

        initialize();

        while(!done)
        {
            // Two conditions can end this loop:
            //    if the maximum number of epochs allowed has been reached, or,
            //    if the Target value has been found.
            if(epoch < MAX_EPOCHS){

                for(int i = 0; i < MAX_PARTICLES; i++)
                {
                    aParticle = particles.get(i);
                    for(int j = 0; j < MAX_INPUTS; j++)
                    {
                        if(j < MAX_INPUTS - 1){
                            System.out.print(aParticle.data(j) + " + ");
                        }else{
                            System.out.print(aParticle.data(j) + " = ");
                        }
                    } // j

                    System.out.print(testProblem(i)+ "\n");
                    if(testProblem(i) == TARGET){
                        done = true;
                    }
                } // i

                gBestTest = minimum();
                aParticle = particles.get(gBest);
                // if(any particle's pBest value is better than the gBest value, make it the new gBest value.
                if(Math.abs(TARGET - testProblem(gBestTest)) < Math.abs(TARGET - testProblem(gBest))){
                    gBest = gBestTest;
                }

                getVelocity(gBest);

                updateparticles(gBest);
                
                System.out.println("epoch number: " + epoch);

                epoch += 1;

            }else{
                done = true;
            }
        }
        return;
    }
    
    private static void getVelocity(int gBestindex)
    {
    //  from Kennedy & Eberhart(1995).
    //    vx[][] = vx[][] + 2 * rand() * (pbestx[][] - presentx[][]) + 
    //                      2 * rand() * (pbestx[][gbest] - presentx[][])

        int testResults = 0;
        int bestResults = 0;
        double vValue = 0.0;
        Particle aParticle = null;

        bestResults = testProblem(gBestindex);

        for(int i = 0; i < MAX_PARTICLES; i++)
        {
            testResults = testProblem(i);
            aParticle = particles.get(i);
            vValue = aParticle.velocity() + 2 * new Random().nextDouble() * (aParticle.pBest() - testResults) + 2 * new Random().nextDouble() * (bestResults - testResults);

            if(vValue > V_MAX){
                aParticle.velocity(V_MAX);
            }else if(vValue < -V_MAX){
                aParticle.velocity(-V_MAX);
            }else{
                aParticle.velocity(vValue);
            }
        }
        return;
    }
    
    private static void updateparticles(int gBestindex)
    {
        Particle gBParticle = particles.get(gBestindex);

        for(int i = 0; i < MAX_PARTICLES; i++)
        {
            for(int j = 0; j < MAX_INPUTS; j++)
            {
                if(particles.get(i).data(j) != gBParticle.data(j)){
                    particles.get(i).data(j, particles.get(i).data(j) + (int)Math.round(particles.get(i).velocity()));
                }
            } // j

            // Check pBest value.
            int total = testProblem(i);
            if(Math.abs(TARGET - total) < particles.get(i).pBest()){
                particles.get(i).pBest(total);
            }

        } // i
        return;
    }
    
    private static int testProblem(int index)
    {
        int total = 0;
        Particle aParticle = null;

        aParticle = particles.get(index);

        for(int i = 0; i < MAX_INPUTS; i++)
        {
            total += aParticle.data(i);
        }
        return total;
    }
    
    private static void printSolution()
    {
        // Find solution particle.
        int i = 0;
        for(; i < particles.size(); i++)
        {
            if(testProblem(i) == TARGET){
                break;
            }
        }
        // Print it.
        System.out.println("Region 2 - Datacenter 1 has been allocated ");
        System.out.println("Particle " + i + " has achieved target.");
        for(int j = 0; j < MAX_INPUTS; j++)
        {
            if(j < MAX_INPUTS - 1){
                System.out.print(particles.get(i).data(j) + " + ");
            }else{
                System.out.print(particles.get(i).data(j) + " = " + TARGET);
                                 
            }
        } // j
        System.out.print("\n");
        return;
    }
    
    private static int getRandomNumber(int low, int high)
    {
        return (int)((high - low) * new Random().nextDouble() + low);
    }
    
    private static int minimum()
    {
    // Returns an array index.
        int winner = 0;
        boolean foundNewWinner = false;
        boolean done = false;

        while(!done)
        {
            foundNewWinner = false;
            for(int i = 0; i < MAX_PARTICLES; i++)
            {
                if(i != winner){             // Avoid self-comparison.
                    // The minimum has to be in relation to the Target.
                    if(Math.abs(TARGET - testProblem(i)) < Math.abs(TARGET - testProblem(winner))){
                        winner = i;
                        foundNewWinner = true;
                    }
                }
            }

            if(foundNewWinner == false){
                done = true;
            }
        }

        return winner;
    }
    
    private static class Particle
    {
        private int mData[] = new int[MAX_INPUTS];
        private int mpBest = 0;
        private double mVelocity = 0.0;
    
        public Particle()
        {
            this.mpBest = 0;
            this.mVelocity = 0.0;
        }
    
        public int data(int index)
        {
            return this.mData[index];
        }
        
        public void data(int index, int value)
        {
            this.mData[index] = value;
            return;
        }
    
        public int pBest()
        {
            return this.mpBest;
        }

        public void pBest(int value)
        {
            this.mpBest = value;
            return;
        }
    
        public double velocity()
        {
            return this.mVelocity;
        }
        
        public void velocity(double velocityScore)
        {
           this.mVelocity = velocityScore;
           return;
        }
    } // Particle
    
    public static void main(String[] args)
    {
        PSOAlgorithm();
        printSolution();
        return;
    }
}
