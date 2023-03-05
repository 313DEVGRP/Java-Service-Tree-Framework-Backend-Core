package com.egovframework.ple.treeframework.springhibernate.service;

import com.egovframework.ple.treeframework.springhibernate.vo.CoreSearchDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CoreService {
	
	public <T extends CoreSearchDTO> T getNode(T coreSearchDTO) throws Exception;

    public <T extends CoreSearchDTO> int updateNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> List<T> getChildNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> List<T> getChildNodeWithoutPaging(T coreSearchDTO) throws Exception;
	
	public <T extends CoreSearchDTO> List<T> getPaginatedChildNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> List<String> searchNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> T addNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> int removeNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> int alterNode(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> int alterNodeType(T coreSearchDTO) throws Exception;

	public <T extends CoreSearchDTO> T moveNode(T coreSearchDTO, HttpServletRequest request) throws Exception;

}
