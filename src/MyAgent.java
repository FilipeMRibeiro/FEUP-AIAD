import jade.core.Agent;

public class MyAgent extends Agent {

	private static final long serialVersionUID = 1L;

	public void setup() {
		System.out.println("Hi! I am " + this.getLocalName());
	}

	public void afterMove() {
		System.out.println(this.getLocalName() + " just moved here.");
	}

}
