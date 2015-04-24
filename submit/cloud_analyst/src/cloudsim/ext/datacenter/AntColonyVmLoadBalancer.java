package cloudsim.ext.datacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cloudsim.VirtualMachine;
import cloudsim.VirtualMachineList;

public class AntColonyVmLoadBalancer extends VmLoadBalancer {
	// Holds the values of pheromones
	private double[][] pheromones;
	static final double alpha = 1;
	static final double beta = 1;
	static final double ONE_UNIT_PHEROMONE = 1;
	static final double EVAPORATION_FACTOR = 2;
	private final int NUM_ANTS = 10;

	Ant[] ants;
	DatacenterController dcbLocal;

	public AntColonyVmLoadBalancer(DatacenterController dcb) {
		super();
		dcbLocal = dcb;
		// ((VirtualMachine)vmList.get(0)).getMemory()
	}

	@Override
	public int getNextAvailableVm() {
		pheromones = new double[dcbLocal.vmlist.size() + 1][dcbLocal.vmlist
				.size() + 1];
		ants = new Ant[NUM_ANTS];
		for (int i = 0; i < ants.length; i++) {
			ants[i] = new Ant(pheromones);
		}
		for (int ant = 0; ant < ants.length; ant++) {
			ants[ant].SendAnt();
			Evaporation();
		}

		Ant queryAnt = new Ant(pheromones);
		return queryAnt.FetchFinalVm();
	}

	// TODO(Dolly)
	public void Evaporation() {
		for (int i = 0; i < pheromones.length; i++) {
			for (int j = 0; j < pheromones.length; j++) {
				pheromones[i][j] /= EVAPORATION_FACTOR;
			}
		}
	}

	public class Ant {
		private double[][] pheromones;
		private boolean[] isVisited;
		public List<Integer> path;
		private int fakeVmId;

		public Ant(double[][] ph) {
			pheromones = ph;
			isVisited = new boolean[pheromones.length];
			fakeVmId = isVisited.length - 1;
			path = new ArrayList<Integer>();
		}

		public int SendAnt() {
			return ProcessAnt(true /* updatePheromones */);
		}

		public int FetchFinalVm() {
			return ((VirtualMachine) dcbLocal.vmlist
					.get(ProcessAnt(false /* updatePheromones */))).getVmId();
		}

		public int ProcessAnt(boolean updatePheromones) {
			int CurrentVmId = fakeVmId;
			int nextVmId = getNextVmNode(CurrentVmId);

			if (updatePheromones) {
				UpdatePheromone(CurrentVmId, nextVmId);
			}
			while (nextVmId != CurrentVmId) {
				path.add(nextVmId);
				CurrentVmId = nextVmId;
				nextVmId = getNextVmNode(CurrentVmId);
				if (updatePheromones) {
					UpdatePheromone(CurrentVmId, nextVmId);
				}
			}

			if (updatePheromones) {
				UpdateGlobalPheromones();
			}

			return CurrentVmId;
		}

		// Assuming vmIds start from 0 and are consecutive.
		// Assumed there is one node that is not visited
		public int getNextVmNode(int vmId) {
			double[] probability = computeProbability(vmId);
			Random rand = new Random();
			double randomization = rand.nextDouble();
			for (int i = 0; i < probability.length; i++) {
				randomization = randomization - probability[i];
				if (randomization <= 0) {
					return i;
				}
			}
			for (int i = 0; i < probability.length; i++) {
				System.out.println("Debug " + probability[i]);
			}
			return -1;
		}

		// Assumes there is at least one node that has note been visited
		public double[] computeProbability(int vmId) {
			double[] probability = new double[pheromones.length - 1];
			double sum = 0.0;
			for (int i = 0; i < probability.length; i++) {
				if (isVisited[i]) {
					probability[i] = 0;
					continue;
				}
				probability[i] = scoreFunction(vmId, i);
				sum += probability[i];
			}

			// Normalize
			for (int i = 0; i < probability.length; i++) {
				probability[i] = probability[i] / sum;
			}
			return probability;
		}

		// TODO(Dolly)
		public void UpdatePheromone(int prevId, int newId) {
			pheromones[prevId][newId] += ONE_UNIT_PHEROMONE;
		}

		// TODO make more optimum.
		public double scoreFunction(int prevVmId, int newVmId) {
			// todo cost factor
			double maxBw = ((VirtualMachine) dcbLocal.vmlist.get(newVmId))
					.getCharacteristics().getBw();
			double currentBw = ((VirtualMachine) dcbLocal.vmlist.get(newVmId))
					.getBw();
			// double requestedBw = cloudlet.getUtilizationOfBw(0);
			return Math.pow(pheromones[prevVmId][newVmId], alpha) + 1.0
					+ (maxBw - currentBw / maxBw);

		}

		// TODO(Dolly): Update global pheromones using path and the overall
		// score - Optional
		public void UpdateGlobalPheromones() {
		}
	}
}
