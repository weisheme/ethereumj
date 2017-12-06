package org.ethereum.vm;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * See {@link org.ethereum.config.net.MainNetConfig} for relevant block numbers
 */
public class VMIllegalOperationsTest {
    private final static long HOMESTEAD_BLOCK = 1_150_000;
    private final static long BYZANTIUM_BLOCK = 4_370_000;

    private ProgramInvokeMockImpl invoke;
    private Config config;

    @Before
    public void setup() {
        invoke = new ProgramInvokeMockImpl();
        config = ConfigFactory.empty()
                .withValue("vm.structured.trace", ConfigValueFactory.fromAnyRef(true));
    }

    @After
    public void tearDown() {
        invoke.getRepository().close();
    }

    /**
     * Assert OpCodes that were introduced at a later stage are actively refused by earlier blockchain configs
     */
    @Test(expected = Program.IllegalOperationException.class)
    public void testStep_delegatecall_denied_before_homestead() {
        execute(config, "DELEGATECALL 0xa0b0",  HOMESTEAD_BLOCK - 1);
    }

    @Test(expected = Program.IllegalOperationException.class)
    public void testStep_revert_denied_before_eip206() {
        execute(config, "REVERT 0xa0b0", BYZANTIUM_BLOCK - 1);
    }

    @Test(expected = Program.IllegalOperationException.class)
    public void testStep_returndatacopy_denied_before_eip211() {
        execute(config, "RETURNDATACOPY 0xa0b0", BYZANTIUM_BLOCK - 1);
    }

    @Test(expected = Program.IllegalOperationException.class)
    public void testStep_returndatasize_denied_before_eip206() {
        execute(config, "RETURNDATASIZE 0xa0b0", BYZANTIUM_BLOCK - 1);
    }

    @Test(expected = Program.IllegalOperationException.class)
    public void testStep_staticcall_denied_before_eip214() {
        execute(config, "STATICCALL 0xa0b0", BYZANTIUM_BLOCK - 1);
    }

    private void execute(Config config, String code, long blockNumber) {
        invoke.setNumber(blockNumber);

        SystemProperties props = new SystemProperties(config);
        VM vm = new VM(props);
        Program program = new Program(compile(code), invoke, null, props);
        program.addListener(System.out::println);

        program.fullTrace();
        vm.step(program);
    }

    private byte[] compile(String code) {
        return new BytecodeCompiler().compile(code);
    }

}
