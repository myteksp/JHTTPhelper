package com.gf.http.entities;

public final class RequestBody {
	public final Object value;
	
	public RequestBody(final Object value){
		this.value = value;
	}

	public static final RequestBody create(final Object value){
		return new RequestBody(value);
	}
	
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RequestBody other = (RequestBody) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public final String toString() {
		return "RequestBody [value=" + value + "]";
	}
}
