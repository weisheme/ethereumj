/*
 * Copyright (c) [2017] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.ethereum.vm;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.codec.binary.Base64;
import org.ethereum.config.SystemProperties;
import org.junit.Test;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class VMUtilsTest {
    @Test
    public void closeQuietly() throws Exception {
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        VMUtils.closeQuietly(() -> wasCalled.set(true));
        assertTrue(wasCalled.get());

        VMUtils.closeQuietly(() -> {
            throw new IOException("Should be handled gracefully");
        });

        try {
            VMUtils.closeQuietly(() -> {
                throw new RuntimeException();
            });
            fail("Only IOException should be handled gracefully.");
        } catch (Exception ignore) { }
    }

    @Test
    public void compress() throws Exception {
        byte[] bytes = VMUtils.compress("the quick brown fox jumped over the lazy dog's back 1234567890");
        String expectedBase64 = "eJwryUhVKCzNTM5WSCrKL89TSMuvUMgqzS1ITVHIL0stUigByuckVlUqpOSnqxcrJCUCVRoaGZuYmplbWBoAAMS8FMg=";
        assertEquals(expectedBase64, Base64.encodeBase64String(bytes));
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotGracefullyHandleNullOnCompress() throws Exception {
        VMUtils.compress((String) null);
    }

    @Test
    public void decompress() throws Exception {
        String base64 = "eJwryUhVKCzNTM5WSCrKL89TSMuvUMgqzS1ITVHIL0stUigByuckVlUqpOSnqxcrJCUCVRoaGZuYmplbWBoAAMS8FMg=";
        byte[] bytes = VMUtils.decompress(Base64.decodeBase64(base64));
        assertEquals("the quick brown fox jumped over the lazy dog's back 1234567890", new String(bytes));
    }

    @Test
    public void shouldGracefullyHandleNonBase64OnUnzipAndDecode() throws Exception {
        String invalidBase64 = "\u5678";
        String emptyString = VMUtils.unzipAndDecode(invalidBase64);
        assertEquals("", emptyString);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotGracefullyHandleNullOnDecompress() throws Exception {
        VMUtils.decompress(null);
    }


    @Test
    public void zipAndEncode() throws Exception {
        String encodedBytes = VMUtils.zipAndEncode("the quick brown fox jumped over the lazy dog's back 1234567890");
        String expectedBase64 = "eJwryUhVKCzNTM5WSCrKL89TSMuvUMgqzS1ITVHIL0stUigByuckVlUqpOSnqxcrJCUCVRoaGZuYmplbWBoAAMS8FMg=";
        assertEquals(expectedBase64, encodedBytes);

        String unalteredValue = VMUtils.zipAndEncode(null);
        assertNull(unalteredValue);
    }

    @Test
    public void unzipAndDecode() throws Exception {
        String base64 = "eJwryUhVKCzNTM5WSCrKL89TSMuvUMgqzS1ITVHIL0stUigByuckVlUqpOSnqxcrJCUCVRoaGZuYmplbWBoAAMS8FMg=";
        String decodedBytes = VMUtils.unzipAndDecode(base64);
        assertEquals("the quick brown fox jumped over the lazy dog's back 1234567890", decodedBytes);

        String unalteredValue = VMUtils.zipAndEncode(null);
        assertNull(unalteredValue);
    }

    @Test
    public void saveProgramTraceFile() throws Exception {
        Config config = ConfigFactory.empty()
                .withValue("vm.structured.trace", ConfigValueFactory.fromAnyRef(true));

        SystemProperties props = new SystemProperties(config);
        String txHash = "123";
        String content = "My content\n";

        File traceFile = VMUtils.constructTraceFilePath(props, txHash);
        //noinspection ResultOfMethodCallIgnored
        traceFile.delete();

        VMUtils.saveProgramTraceFile(props, txHash, content);
        assertFileContentsMatch(content, traceFile);

        // Verify that trace files are truncated before appending content
        VMUtils.saveProgramTraceFile(props, txHash, content);
        assertFileContentsMatch(content, traceFile);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveProgramTraceFile_ignoreReadOnlyFiles() throws Exception {
        Config config = ConfigFactory.empty()
                .withValue("vm.structured.trace", ConfigValueFactory.fromAnyRef(true));

        SystemProperties props = new SystemProperties(config);
        String txHash = "write_only_123";
        String content = "Some content that can never be written";

        File traceFile = VMUtils.constructTraceFilePath(props, txHash);
        traceFile.delete();
        traceFile.createNewFile();
        traceFile.setReadOnly();

        VMUtils.saveProgramTraceFile(props, txHash, content);
        assertFileContentsMatch("", traceFile);
    }

    private void assertFileContentsMatch(String content, File traceFile) throws IOException {
        String programTrace = new String(Files.readAllBytes(traceFile.toPath()));
        assertEquals(content, programTrace);
    }
}