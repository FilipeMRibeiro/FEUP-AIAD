import agents.SimpleAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.SimInit;

public class MyLauncher extends Repast3Launcher {

	private ContainerController mainContainer;

	@Override
	public String[] getInitParam() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "SAJaS Project";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		launchAgents();
	}

	private void launchAgents() {
		try {

			// TODO
			mainContainer.acceptNewAgent("Bot1", new SimpleAgent()).start();
			// ...

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launching Repast3
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean BATCH_MODE = true;
		SimInit init = new SimInit();
		init.setNumRuns(1); // works only in batch mode
		init.loadModel(new MyLauncher(), null, BATCH_MODE);
	}

}
