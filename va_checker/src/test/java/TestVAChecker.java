import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class TestVAChecker {
    // TODO feed test text to parser, check if validate produces appropriate errors
    // TODO check out : TestNG, Selenium, Selenide

    // @Test is an annotation (=python decorator), that marks method as test method -> convention.
    @org.junit.jupiter.api.Test
    public void testNodesSize() {
        assertEquals(0, Gen.nodes.size());
        Gen.setNodes();
        assertFalse(Gen.nodes.isEmpty());

        int amount = Gen.nodes.size();
        Gen.setNodes();
        assertEquals(amount, Gen.nodes.size());
    }

    // This test serves to perform a meta test of JUnit using testSomeTest.
    // .setNodes only adds nodes if Gen.nodes is empty, so this test will fail, which is intended!.
    @org.junit.jupiter.api.Test
    public void testNodesSizeMeta() {
        assertEquals(0, Gen.nodes.size());
        Gen.setNodes();
        assertFalse(Gen.nodes.isEmpty());

        int amount = Gen.nodes.size();
        Gen.setNodes();
        assertEquals(Gen.nodes.size()+1, amount); //  +1 to cause intended fail of the test
    }

    @org.junit.jupiter.api.Test
    public void testNodeNumberOfChildren() {
        Gen.setNodes();
        for (Nodes node : Gen.nodes) {
            assertEquals(0, node.children.size(), "init number of children:");
            //assertEquals(Integer.parseInt("init number of children: ", ),0, node.children.size());
        }
        Gen.setParents();
        for (Nodes node : Gen.nodes) {
            if(node.parent) {
                assertFalse(node.children.isEmpty());
            }
        }	// TODO: reverse path (isParent...) for complete coverage
    }

    // tests if children of a parent nodes have the appropriate key -> (<parentKey>.*)
    @org.junit.jupiter.api.Test
    public void testChildrenKeys() {
        Gen.setNodes();
        Gen.setParents();
        for (Nodes node : Gen.nodes) {
            if(node.parent) {
                for(Nodes child : node.children) {
                    assertTrue(child.key.contains(node.key + "."));
                }
            }
        }
    }

    // supposed to test a test by asserting the test to be tested fails, which it does.
    // so testSomeTest is successful because it expects testNodesSizeB to fail
    // So this test doesn't test VAPrüfer but JUnit itself!
    @org.junit.jupiter.api.Test
    void testSomeTest() {
        assertThrows(AssertionError.class, this::testNodesSizeMeta);  // this::function_name -> callback
    }

    // @BeforeEach is an annotation (=decorator in python). Since test methods are not executed in order
    // annotation tells IDE to run init() first. Does not show up in JUnit summary. -> usually used for reset methods.
    @BeforeEach
    public void init()
    {
        Gen.nodes.clear();
    }
}


