package com.adesis.poc.gitdb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Constants;

public class GitHelper {

	public static Git cloneRepository(String repoUri, String targetPath) throws InvalidRemoteException, TransportException, GitAPIException {
		File directory = new File(targetPath);
		if (directory.exists()) {
			throw new IllegalArgumentException("Target path already exists.");
		}
		return Git
			.cloneRepository()
			.setURI(repoUri)
			.setDirectory(directory)
			.setBranch("master")
			.setBare(false)
			.setRemote("origin")
			.setNoCheckout(false).call();
	}

	public static Git cloneAndBranchRepository(String repoUri, String targetPath, String branchName) throws InvalidRemoteException, TransportException, GitAPIException {
		File directory = new File(targetPath);
		if (directory.exists()) {
			throw new IllegalArgumentException("Target path already exists.");
		}
		Git git = cloneRepository(repoUri, targetPath);
		git.branchCreate() 
	       .setName( branchName )
	       .call(); 
		git.checkout().setName( "refs/heads/" + branchName ).call();
		return git;
	}

	public static void createBareRepository(String bareRepoPath) throws GitAPIException {
		Git
			.init()
			.setDirectory(new File(bareRepoPath))
			.setBare(true)
			.call();
	}

	public static String getRepoUri(String accountId) {
		return "/tmp/account-bare/" + accountId + Constants.DOT_GIT_EXT;
	}

	public static void pushToRemoteRepository(final Git git, String commitMessage) throws GitAPIException,
																							NoFilepatternException, NoHeadException, NoMessageException,
																							UnmergedPathsException, ConcurrentRefUpdateException,
																							WrongRepositoryStateException, InvalidRemoteException,
																							TransportException, IOException {
		git.add().addFilepattern(".").call();
		git.commit().setMessage(commitMessage).call();
		git.push().setPushAll().setRemote("origin").call();
		git.close();
		FileUtils.deleteDirectory( git.getRepository().getDirectory() );
	}

}
