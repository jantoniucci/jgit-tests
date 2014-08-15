package com.adesis.poc.gitdb;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Constants;

public class ProductGitHelper {

	public static Git cloneRepository(String productId) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File directory = new File(getClonePath(productId));
		if (directory.exists()) {
			return pullRepository(productId);
		} else {
			return cloneRepository(productId, getClonePath(productId));
		}
	}

	private static Git cloneRepository(String productId, String targetPath) throws InvalidRemoteException, TransportException, GitAPIException {
		return Git
			.cloneRepository()
			.setURI(getRepoUri(productId))
			.setDirectory( new File(targetPath) )
			.setBranch("master")
			.setBare(false)
			.setRemote("origin")
			.setNoCheckout(false)
			.call();
	}

	public static Git cloneAndBranchRepository(String productId, String branchName) throws InvalidRemoteException, TransportException, GitAPIException {
		Git git = cloneRepository(productId, getBranchPath(productId, branchName));
		git.branchCreate() 
	       .setName( branchName )
	       .call(); 
		git.checkout().setName( "refs/heads/" + branchName ).call();
		return git;
	}

	public static void createBareRepository(String productId) throws GitAPIException {
		Git
			.init()
			.setDirectory(new File(getRepoUri(productId)))
			.setBare(true)
			.call();
	}

	private static String getRepoUri(String productId) {
		return "/tmp/account-bare/" + productId + Constants.DOT_GIT_EXT;
	}

	private static String getClonePath(String productId) {
		return "/tmp/account-clones/" + productId;
	}

	private static String getBranchPath(String productId, String branchName) {
		return getClonePath(productId) + "-branch-" + branchName + "-" + UUID.randomUUID().toString();
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
	}

	private static Git pullRepository(String productId) throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException, TransportException, GitAPIException {
		Git git = Git.open( new File( getClonePath(productId) + "/" + Constants.DOT_GIT_EXT) );
		git.pull().call();
		return git;
	}

}
