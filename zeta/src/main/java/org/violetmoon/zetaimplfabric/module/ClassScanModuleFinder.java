package org.violetmoon.zetaimplfabric.module;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.violetmoon.zeta.module.ModuleFinder;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaLoadModuleAnnotationData;
import org.violetmoon.zeta.module.ZetaModule;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Fabric replacement for Forge's ModFileScanDataModuleFinder: scans the class files of a mod for
 * {@link ZetaLoadModule} annotated classes.
 */
public class ClassScanModuleFinder implements ModuleFinder {

	private static final byte[] DESCRIPTOR = ("L" + ZetaLoadModule.class.getName().replace('.', '/') + ";").getBytes(StandardCharsets.UTF_8);

	private final String modid;
	private final String packagePrefix;

	/**
	 * @param modid         the mod whose files should be scanned
	 * @param packagePrefix only classes in this package (and subpackages) are considered, e.g. "org.violetmoon.quark"
	 */
	public ClassScanModuleFinder(String modid, String packagePrefix) {
		this.modid = modid;
		this.packagePrefix = packagePrefix;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<ZetaLoadModuleAnnotationData> get() {
		ModContainer container = FabricLoader.getInstance().getModContainer(modid)
			.orElseThrow(() -> new IllegalStateException("No mod container for " + modid));

		List<String> classNames = new ArrayList<>();
		String packagePath = packagePrefix.replace('.', '/');
		for(Path root : container.getRootPaths()) {
			Path start = root.resolve(packagePath);
			if(!Files.isDirectory(start))
				continue;

			try(Stream<Path> files = Files.walk(start)) {
				files.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
					if(containsDescriptor(p)) {
						String rel = root.relativize(p).toString().replace('\\', '/');
						classNames.add(rel.substring(0, rel.length() - ".class".length()).replace('/', '.'));
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException("Failed scanning " + modid + " for Zeta modules", e);
			}
		}

		ClassLoader loader = ClassScanModuleFinder.class.getClassLoader();
		return classNames.stream().map(name -> {
			Class<?> clazz;
			try {
				clazz = Class.forName(name, false, loader);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Exception getting ZetaModule class " + name, e);
			}

			ZetaLoadModule annotation = clazz.getAnnotation(ZetaLoadModule.class);
			if(annotation == null || !ZetaModule.class.isAssignableFrom(clazz))
				return null; // only mentioned the annotation, not annotated with it

			return ZetaLoadModuleAnnotationData.fromAnnotation((Class<? extends ZetaModule>) clazz, annotation);
		}).filter(d -> d != null);
	}

	private static boolean containsDescriptor(Path classFile) {
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(classFile);
		} catch (IOException e) {
			return false;
		}

		outer:
		for(int i = 0; i <= bytes.length - DESCRIPTOR.length; i++) {
			for(int j = 0; j < DESCRIPTOR.length; j++)
				if(bytes[i + j] != DESCRIPTOR[j])
					continue outer;
			return true;
		}
		return false;
	}
}
