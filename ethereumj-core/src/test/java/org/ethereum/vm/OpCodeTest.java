package org.ethereum.vm;

import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

public class OpCodeTest {

    @Test
    public void contains() {
        for (OpCode opCode : OpCode.values()) {
            assertTrue(OpCode.contains(opCode.name()));
        }
        assertFalse(OpCode.contains("call"));
        assertFalse(OpCode.contains("Call"));
        assertFalse(OpCode.contains("\uac20CALL"));
        assertTrue(OpCode.contains("CALL "));
        assertTrue(OpCode.contains(" CALL"));
        assertTrue(OpCode.contains("\tCALL"));
        assertTrue(OpCode.contains("\tCALL\n"));
    }

    @Test
    public void code() {
        // Make sure all opcodes are accounted for
        for (OpCode opCode : OpCode.values()) {
            assertEquals(opCode, OpCode.code(opCode.val()));
        }
        // Random non-existent OpCodes
        assertNull(OpCode.code((byte) 0x5e));
        assertNull(OpCode.code((byte) 0x1b));
        assertNull(OpCode.code((byte) 0x2b));
    }

    @Test
    public void isCall() {
        Set<OpCode> knownCalls = newHashSet(
                OpCode.CALL,
                OpCode.CALLCODE,
                OpCode.DELEGATECALL,
                OpCode.STATICCALL
        );
        for (OpCode opCode : OpCode.values()) {
            boolean shouldBeCall = knownCalls.contains(opCode);
            boolean isCall = opCode.isCall();
            assertEquals(opCode + " should" + (shouldBeCall ? " " : " not ") + "be a call", shouldBeCall, isCall);
        }
    }

    @Test
    public void callIsStateless() {
        Set<OpCode> knownStatelessCalls = newHashSet(
                OpCode.CALLCODE,
                OpCode.DELEGATECALL
        );
        for (OpCode opCode : OpCode.values()) {
            boolean shouldBeStateless = knownStatelessCalls.contains(opCode);
            try {
                boolean isStateless = opCode.callIsStateless();
                assertEquals(opCode + " should" + (shouldBeStateless ? " " : " not ") + "be a stateless call", shouldBeStateless, isStateless);
            } catch (Exception thrownWhenOpCodeIsNotACall) {
                if (shouldBeStateless) {
                    throw thrownWhenOpCodeIsNotACall;
                }
            }
        }
    }

    @Test
    public void callHasValue() {
        Set<OpCode> knownHasValue = newHashSet(
                OpCode.CALL,
                OpCode.CALLCODE
        );
        for (OpCode opCode : OpCode.values()) {
            boolean shouldHaveValue = knownHasValue.contains(opCode);
            try {
                boolean callHasValue = opCode.callHasValue();
                assertEquals(opCode + " should" + (shouldHaveValue ? " " : " not ") + "have value", shouldHaveValue, callHasValue);
            } catch (Exception thrownWhenOpCodeIsNotACall) {
                if (shouldHaveValue) {
                    throw thrownWhenOpCodeIsNotACall;
                }
            }
        }
    }

    @Test
    public void callIsStatic() {
        Set<OpCode> knownHasValue = newHashSet(
                OpCode.STATICCALL
        );
        for (OpCode opCode : OpCode.values()) {
            boolean shouldBeStatic = knownHasValue.contains(opCode);
            try {
                boolean callIsStatic = opCode.callIsStatic();
                assertEquals(opCode + " should" + (shouldBeStatic ? " " : " not ") + " be static", shouldBeStatic, callIsStatic);
            } catch (Exception thrownWhenOpCodeIsNotACall) {
                if (shouldBeStatic) {
                    throw thrownWhenOpCodeIsNotACall;
                }
            }
        }
    }

    @Test
    public void callIsDelegate() {
        Set<OpCode> knownHasValue = newHashSet(
                OpCode.DELEGATECALL
        );
        for (OpCode opCode : OpCode.values()) {
            boolean shouldBeDelegate = knownHasValue.contains(opCode);
            try {
                boolean callIsDelegate = opCode.callIsDelegate();
                assertEquals(opCode + " should" + (shouldBeDelegate ? " " : " not ") + " delegate call", shouldBeDelegate, callIsDelegate);
            } catch (Exception thrownWhenOpCodeIsNotACall) {
                if (shouldBeDelegate) {
                    throw thrownWhenOpCodeIsNotACall;
                }
            }
        }
    }
}