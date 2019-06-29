package com.icefire.api.common.rest;

import org.springframework.hateoas.Link;
import org.springframework.http.HttpMethod;

import javax.xml.bind.annotation.XmlType;

@SuppressWarnings("ALL")
@XmlType(name = "_xlink", namespace = Link.ATOM_NAMESPACE)
public class ExtendedLink extends Link {
	private static final long serialVersionUID = -9037755944661782122L;
	private HttpMethod method;
	private String _rel;
	
	protected ExtendedLink(){}
	
	public ExtendedLink(String href, String rel, HttpMethod method){
		super(href, rel);
		this.method = method;
		this._rel = rel;
	}
	
	public HttpMethod getMethod(){
		return method;
	}

	public String get_rel() { return _rel; }
}
