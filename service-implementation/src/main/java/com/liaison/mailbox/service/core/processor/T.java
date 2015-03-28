package com.liaison.mailbox.service.core.processor;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class T {
	
	public static void main(String[] args) {
		FileSystem fileSystem = FileSystems.getDefault();
		PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:**");
		Path path = Paths.get("D:/cp/testFile.t");
		System.out.println(pathMatcher.matches(path));
	}

}
