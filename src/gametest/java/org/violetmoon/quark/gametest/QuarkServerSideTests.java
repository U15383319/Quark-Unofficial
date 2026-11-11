package org.violetmoon.quark.gametest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Checks that only meaningfully fail on a production dedicated server (runProductionGametest), where client classes
 * don't exist. In the development environment they always pass.
 */
public class QuarkServerSideTests implements FabricGameTest {

	/**
	 * Every class of Quark and Zeta outside client code must load on a dedicated server. Fabric strips
	 * {@code @Environment(CLIENT)} methods there, but not the lambdas inside them (Forge strips those too), and one
	 * that uses client classes keeps its whole class from loading: that's how Quark's emotes stopped working on
	 * servers (DoEmoteMessage could never be sent). Client code is recognised by name: a ".client." package, a
	 * "Client" nested class (Module$Client), a *ClientDefinition config screen, or listed in CLIENT_ONLY.
	 */
	@GameTest(template = EMPTY_STRUCTURE)
	public void commonClassesLoadOnServer(GameTestHelper helper) {
		ClassLoader loader = QuarkServerSideTests.class.getClassLoader();
		TreeMap<String, String> broken = new TreeMap<>();
		int checked = 0;
		for(String mod : List.of("quark", "zeta", "quarkoddities")) {
			for(String name : classNames(mod)) {
				if(isClientOnly(name) || name.contains(".mixin.") || name.endsWith("package-info"))
					continue;
				checked++;
				try {
					Class<?> clazz = Class.forName(name, false, loader);
					clazz.getDeclaredMethods(); // links the class, which verifies its bytecode
				} catch(Throwable t) {
					broken.put(name, t.toString());
				}
			}
		}
		helper.assertTrue(checked > 500, "only found " + checked + " classes to check");
		helper.assertTrue(broken.isEmpty(), broken.size() + " class(es) don't load on a dedicated server: " + broken);
		helper.succeed();
	}

	// client-only classes whose names don't say so
	private static final Set<String> CLIENT_ONLY = Set.of(
		"org.violetmoon.zetaimplfabric.network.FabricZetaClientNetworking" // only used when Zeta runs on a client
	);

	private static boolean isClientOnly(String name) {
		return name.contains(".client.") || name.contains("$Client") || name.contains("ClientDefinition") || CLIENT_ONLY.contains(name);
	}

	private static List<String> classNames(String mod) {
		List<String> names = new ArrayList<>();
		var container = FabricLoader.getInstance().getModContainer(mod); // Quark Woven Oddities isn't separate in development
		if(container.isEmpty())
			return names;
		for(Path root : container.get().getRootPaths()) {
			String separator = root.getFileSystem().getSeparator();
			try(Stream<Path> walk = Files.walk(root)) {
				walk.map(p -> root.relativize(p).toString())
					.filter(p -> p.endsWith(".class"))
					.forEach(p -> names.add(p.substring(0, p.length() - 6).replace(separator, ".")));
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		return names;
	}
}
