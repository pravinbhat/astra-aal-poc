package com.bhatman.poc.astra.flight;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

public class SysLocalHistCodec extends MappingCodec<UdtValue, SysLocalHist> {

	  public SysLocalHistCodec(@NonNull TypeCodec<UdtValue> innerCodec) {
	    super(innerCodec, GenericType.of(SysLocalHist.class));
	  }

	  @NonNull @Override public UserDefinedType getCqlType() {
	    return (UserDefinedType) super.getCqlType();
	  }

	  @Nullable @Override protected SysLocalHist innerToOuter(@Nullable UdtValue value) {
	    return value == null ? null : new SysLocalHist(value.getString("syshealth"));
	  }

	  @Nullable @Override protected UdtValue outerToInner(@Nullable SysLocalHist value) {
	    return value == null ? null : getCqlType().newValue().setString("syshealth", value.syshealth);
	  }
}