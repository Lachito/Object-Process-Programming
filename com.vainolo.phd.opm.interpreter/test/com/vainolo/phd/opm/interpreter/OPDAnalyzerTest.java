package com.vainolo.phd.opm.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.draw2d.geometry.Rectangle;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vainolo.phd.opm.model.OPMFactory;
import com.vainolo.phd.opm.model.OPMObjectProcessDiagram;
import com.vainolo.phd.opm.model.OPMProcess;

/**
 * The class <code>OPDAnalyzerTest</code> contains tests for the class <code>{@link OPDAnalyzer}</code>.
 * 
 * @generatedBy CodePro at 6/27/12 11:34 AM
 * @author vainolo
 * @version $Revision: 1.0 $
 */
public class OPDAnalyzerTest {
	OPMObjectProcessDiagram opd;
	DirectedAcyclicGraph<OPMProcess, DefaultEdge> opdDag;
	private static final int SPACING = 50;

	private final Map<Integer, OPMProcess> processes = new HashMap<Integer, OPMProcess>();
	private List<OPMProcess> nextProcesses;

	private void createProcess(final Integer name) {
		OPMProcess process = OPMFactory.eINSTANCE.createOPMProcess();
		process.setName(name.toString());
		process.setConstraints(new Rectangle(10, 10, 10, 10));
		processes.put(name, process);
		opd.getNodes().add(process);
	}

	private void createParalleProcess(final OPMProcess process, final Integer name) {
		OPMProcess newProcess = OPMFactory.eINSTANCE.createOPMProcess();
		newProcess.setName(name.toString());
		Rectangle constraints = new Rectangle();
		constraints.setX(process.getConstraints().x() + SPACING);
		constraints.setY(process.getConstraints().y());
		constraints.setSize(process.getConstraints().getSize().getCopy());
		newProcess.setConstraints(constraints);
		processes.put(name, newProcess);
		opd.getNodes().add(newProcess);
	}

	private void createSerialProcess(final OPMProcess process, final Integer name) {
		OPMProcess newProcess = OPMFactory.eINSTANCE.createOPMProcess();
		newProcess.setName(name.toString());
		Rectangle constraints = new Rectangle();
		constraints.setX(process.getConstraints().x());
		constraints.setY(process.getConstraints().getBottom().y() + SPACING);
		constraints.setSize(process.getConstraints().getSize().getCopy());
		newProcess.setConstraints(constraints);
		processes.put(name, newProcess);
		opd.getNodes().add(newProcess);
	}

	/**
	 * @generatedBy CodePro at 6/27/12 11:34 AM
	 */
	@Test
	public void testCreateOPDDAG_DAGNeverNull() {
		opd = OPMFactory.eINSTANCE.createOPMObjectProcessDiagram();
		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertNotNull(opdDag);
	}

	@Test
	public void testCreateOPDDAG_OneNode() {
		createProcess(1);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertEquals(1, opdDag.vertexSet().size());
		assertTrue(opdDag.vertexSet().contains(processes.get(1)));
	}

	@Test
	public void testCreateOPDDAG_TwoNodes() {
		final int numOfNodes = 2;
		for(int i = 1; i <= numOfNodes; i++)
			createProcess(i);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertEquals(numOfNodes, opdDag.vertexSet().size());
		assertTrue(opdDag.vertexSet().contains(processes.get(1)));
		assertTrue(opdDag.vertexSet().contains(processes.get(2)));
	}

	@Test
	public void testCreateOPDDAG_RandomNumberOfNodes() {
		final int numOfNodes = (new Random()).nextInt(50);
		for(int i = 1; i <= numOfNodes; i++)
			createProcess(i);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertEquals(numOfNodes, opdDag.vertexSet().size());
		for(int i = 1; i <= numOfNodes; i++)
			assertTrue(opdDag.vertexSet().contains(processes.get(i)));
	}

	@Test
	public void testCreateOPDDAG_TwoSerialNodes() {
		createProcess(1);
		createSerialProcess(processes.get(1), 2);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		final Set<DefaultEdge> edges = opdDag.getAllEdges(processes.get(1), processes.get(2));
		assertEquals(1, edges.size());
		final DefaultEdge edge = edges.iterator().next();
		assertEquals(processes.get(1), opdDag.getEdgeSource(edge));
		assertEquals(processes.get(2), opdDag.getEdgeTarget(edge));
	}

	@Test
	public void testCreateOPDDAG_TwoParallelNodes() {
		createProcess(1);
		createParalleProcess(processes.get(1), 2);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		final Set<DefaultEdge> edges = opdDag.getAllEdges(processes.get(1), processes.get(2));
		assertEquals(0, edges.size());
	}

	@Test
	public void testCreateOPDDAG_OneAndThenTwoParallel() {
		createProcess(1);
		createSerialProcess(processes.get(1), 2);
		createParalleProcess(processes.get(2), 3);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertTrue(opdDag.containsEdge(processes.get(1), processes.get(2)));
		assertTrue(opdDag.containsEdge(processes.get(1), processes.get(3)));
		assertTrue(!opdDag.containsEdge(processes.get(2), processes.get(3)));
		assertTrue(!opdDag.containsEdge(processes.get(3), processes.get(2)));
	}

	@Test
	public void testCreateOPDDAG_TwoParallelAndThenOne() {
		createProcess(1);
		createParalleProcess(processes.get(1), 2);
		createSerialProcess(processes.get(1), 3);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertEquals(2, opdDag.edgeSet().size());
		assertTrue(opdDag.containsEdge(processes.get(1), processes.get(3)));
		assertTrue(opdDag.containsEdge(processes.get(2), processes.get(3)));
		assertTrue(!opdDag.containsEdge(processes.get(1), processes.get(2)));
		assertTrue(!opdDag.containsEdge(processes.get(2), processes.get(1)));
	}

	@Test
	public void testCreateOPDDAG_SerialNoDuplicateEdges() {
		createProcess(1);
		createSerialProcess(processes.get(1), 2);
		createSerialProcess(processes.get(2), 3);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertEquals(2, opdDag.edgeSet().size());
		assertTrue(opdDag.containsEdge(processes.get(1), processes.get(2)));
		assertTrue(opdDag.containsEdge(processes.get(2), processes.get(3)));
	}

	@Test
	public void testCreateDAG_SerialAndParallelNoDuplicates() {
		createProcess(1);
		createSerialProcess(processes.get(1), 2);
		createParalleProcess(processes.get(2), 3);
		createSerialProcess(processes.get(3), 4);

		opdDag = OPDAnalyzer.createOPDDAG(opd);

		assertEquals(4, opdDag.edgeSet().size());
		assertTrue(opdDag.containsEdge(processes.get(1), processes.get(2)));
		assertTrue(opdDag.containsEdge(processes.get(1), processes.get(3)));
		assertTrue(opdDag.containsEdge(processes.get(2), processes.get(4)));
		assertTrue(opdDag.containsEdge(processes.get(3), processes.get(4)));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetNextProcessToExecute_NullDAG_IllegalArgumentException() {
		createProcess(1);
		OPDAnalyzer.getNextProcessesToExecute(null, processes.get(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetNextProcessToExecute_NullProcess_IllegalArgumentException() {
		opdDag = OPDAnalyzer.createOPDDAG(opd);

		OPDAnalyzer.getNextProcessesToExecute(opdDag, null);
	}

	private void prepareSerialOPDTest() {
		createProcess(1);
		createSerialProcess(processes.get(1), 2);
		createSerialProcess(processes.get(2), 3);
		createSerialProcess(processes.get(3), 4);
		opdDag = OPDAnalyzer.createOPDDAG(opd);
	}

	@Test
	public void testGetNextProcessToExecute_SerialOPD_1() {
		prepareSerialOPDTest();

		List<OPMProcess> nextProcesses = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(1));

		assertEquals(1, nextProcesses.size());
		assertTrue(nextProcesses.contains(processes.get(2)));
	}

	@Test
	public void testGetNextProcessToExecute_SerialOPD_2() {
		prepareSerialOPDTest();

		nextProcesses = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(3));

		assertEquals(1, nextProcesses.size());
		assertTrue(nextProcesses.contains(processes.get(4)));
	}

	@Test
	public void testGetNextProcessToExecute_SerialOPD_3() {
		prepareSerialOPDTest();

		nextProcesses = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(4));

		assertEquals(0, nextProcesses.size());
	}

	private void prepareParallelOPDTest() {
		createProcess(1);
		createSerialProcess(processes.get(1), 2);
		createParalleProcess(processes.get(2), 3);
		createParalleProcess(processes.get(3), 4);
		createSerialProcess(processes.get(2), 5);
		createSerialProcess(processes.get(3), 6);
		createSerialProcess(processes.get(5), 7);
		opdDag = OPDAnalyzer.createOPDDAG(opd);
	}

	@Test
	public void testGetNextProcessToExecute_ParallelOPD_1() {
		prepareParallelOPDTest();

		nextProcesses = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(1));

		assertEquals(3, nextProcesses.size());
		assertTrue(nextProcesses.contains(processes.get(2)));
		assertTrue(nextProcesses.contains(processes.get(3)));
		assertTrue(nextProcesses.contains(processes.get(4)));
	}

	@Test
	public void testGetNextProcessToExecute_ParallelOPD_2() {
		prepareParallelOPDTest();

		nextProcesses = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(2));

		assertEquals(2, nextProcesses.size());
		assertTrue(nextProcesses.contains(processes.get(5)));
		assertTrue(nextProcesses.contains(processes.get(6)));
	}

	@Test
	public void textGetNextProcessToExecute_ParallelOPD_3() {
		prepareParallelOPDTest();

		nextProcesses = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(5));

		List<OPMProcess> nextProcesses2 = OPDAnalyzer.getNextProcessesToExecute(opdDag, processes.get(6));
		assertEquals(nextProcesses, nextProcesses2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetInitialProcesses_EmptyOPD() {
		OPDAnalyzer.createOPDDAG(opd);
		List<OPMProcess> initialProcesses = OPDAnalyzer.getInitialProcesses(opdDag);
		assertEquals(0, initialProcesses.size());
	}

	@Test
	public void testGetInitialProcesses_OneProcess() {
		createProcess(1);
		opdDag = OPDAnalyzer.createOPDDAG(opd);
		List<OPMProcess> initialProcesses = OPDAnalyzer.getInitialProcesses(opdDag);
		assertEquals(1, initialProcesses.size());
	}

	@Test
	public void testGetInitialProcesses_TwoParallelProcesses() {
		createProcess(1);
		createParalleProcess(processes.get(1), 2);
		createSerialProcess(processes.get(1), 3);
		createSerialProcess(processes.get(2), 4);
		createSerialProcess(processes.get(3), 5);
		createSerialProcess(processes.get(5), 6);
		createParalleProcess(processes.get(5), 7);
		opdDag = OPDAnalyzer.createOPDDAG(opd);
		List<OPMProcess> initialProcesses = OPDAnalyzer.getInitialProcesses(opdDag);
		assertEquals(2, initialProcesses.size());
		assertTrue(initialProcesses.contains(processes.get(1)));
		assertTrue(initialProcesses.contains(processes.get(2)));
	}

	/**
	 * Perform pre-test initialization.
	 * 
	 * @throws Exception
	 *             if the initialization fails for some reason
	 * 
	 * @generatedBy CodePro at 6/27/12 11:34 AM
	 */
	@Before
	public void setUp() throws Exception {
		opd = OPMFactory.eINSTANCE.createOPMObjectProcessDiagram();
	}

	/**
	 * Perform post-test clean-up.
	 * 
	 * @throws Exception
	 *             if the clean-up fails for some reason
	 * 
	 * @generatedBy CodePro at 6/27/12 11:34 AM
	 */
	@After
	public void tearDown() throws Exception {
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 * 
	 * @param args
	 *            the command line arguments
	 * 
	 * @generatedBy CodePro at 6/27/12 11:34 AM
	 */
	public static void main(final String[] args) {
		new org.junit.runner.JUnitCore().run(OPDAnalyzerTest.class);
	}
}