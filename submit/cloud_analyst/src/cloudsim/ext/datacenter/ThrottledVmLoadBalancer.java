package cloudsim.ext.datacenter;

import java.util.Iterator;
import java.util.Map;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;


public class ThrottledVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener {
	
	private Map<Integer, VirtualMachineState> vmStatesList;
	
	/** 
	 * Constructor
	 * 
	 * @param dcb The {@link DatacenterController} using the load balancer.
	 */
	public ThrottledVmLoadBalancer(DatacenterController dcb){
		this.vmStatesList = dcb.getVmStatesList();
		dcb.addCloudSimEventListener(this);
	}

	/**
	 * @return VM id of the first available VM from the vmStatesList in the calling
	 * 			{@link DatacenterController}
	 */
	@Override
	public int getNextAvailableVm(){
		int vmId = -1;
		
		if (vmStatesList.size() > 0){
			int temp;
			for (Iterator<Integer> itr = vmStatesList.keySet().iterator(); itr.hasNext();){
				temp = itr.next();
				VirtualMachineState state = vmStatesList.get(temp); //System.out.println(temp + " state is " + state + " total vms " + vmStatesList.size());
				if (state.equals(VirtualMachineState.AVAILABLE)){
					vmId = temp;
					break;
				}
			}
		}
		
		allocatedVm(vmId);
		
		return vmId;
		
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			vmStatesList.put(vmId, VirtualMachineState.BUSY);
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
		}
	}
	

}
