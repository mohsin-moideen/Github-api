package com.blueoptima.github.repository;

/**
 * POJO class for Repository
 * @author MohsinM
 *
 */
public class Repository {

	private String name;
	private Long contributions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getContributions() {
		return contributions;
	}

	public void setContributions(Long contributions) {
		this.contributions = contributions;
	}
}
