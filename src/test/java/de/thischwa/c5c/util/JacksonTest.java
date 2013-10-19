package de.thischwa.c5c.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonTest {

	private ObjectMapper mapper = new ObjectMapper(); 
	private Map<String, Object> data = new LinkedHashMap<>();
	private DataHolder dataHolder = new DataHolder();
	
	@Before
	public void init() {
		data.put("name", "john");
		data.put("key", Boolean.TRUE);
		
		dataHolder.setName("john");
		dataHolder.setKey(Boolean.TRUE);
	}
	
	@Test
	public void testMapBool() throws Exception {
		String dataStr = mapper.writeValueAsString(data);
		assertEquals("{\"name\":\"john\",\"key\":true}", dataStr);
	}

	@Test
	public void testMapStr() throws Exception {
		data.put("key", "value");
		String dataStr = mapper.writeValueAsString(data);
		assertEquals("{\"name\":\"john\",\"key\":\"value\"}", dataStr);
	}
	
	@Test
	public void testObjBool() throws Exception {
		String dataStr = mapper.writeValueAsString(dataHolder);
		assertEquals("{\"name\":\"john\",\"key\":true}", dataStr);
	}

	@Test
	public void testReadObjBool() throws Exception {
		DataHolder dh = mapper.readValue("{\"name\":\"john\",\"key\":true}", DataHolder.class);
		assertEquals("john", dh.getName());
		assertTrue(dh.getKey() instanceof Boolean);
		assertEquals(Boolean.TRUE, dh.getKey());
	}
	
	@Test
	public void testReadObjStr() throws Exception {
		DataHolder dh = mapper.readValue("{\"name\":\"john\",\"key\":\"value\"}", DataHolder.class);
		assertEquals("john", dh.getName());
		assertTrue(dh.getKey() instanceof String);
		assertEquals("value", dh.getKey());
	}

	@Test
	public void testObjStr() throws Exception {
		dataHolder.setKey("value");
		String dataStr = mapper.writeValueAsString(dataHolder);
		assertEquals("{\"name\":\"john\",\"key\":\"value\"}", dataStr);
	}
	
	@Test
	public void testReadList() throws Exception {
		@SuppressWarnings("unchecked")
		List<String> list = mapper.readValue("[\"a\",\"b\"]", ArrayList.class);
		assertTrue(list.get(0).equals("a"));
		assertTrue(list.get(1).equals("b"));
	}
	
	@Test
	public void testWriteList() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		String dataStr = mapper.writeValueAsString(list);
		assertEquals("[\"a\",\"b\"]", dataStr);
	}
}