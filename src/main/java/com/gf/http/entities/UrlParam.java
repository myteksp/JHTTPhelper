package com.gf.http.entities;

public final class UrlParam {
	public final String name;
	public final String value;
	
	public UrlParam(final String name, final String value){
		this.name = name;
		this.value = value;
	}
	
	public final int length(){
		return name.length() + value.length() + 1;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final UrlParam other = (UrlParam) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public final String toString() {
		return "UrlParam [name=" + name + ", value=" + value + "]";
	}
}
