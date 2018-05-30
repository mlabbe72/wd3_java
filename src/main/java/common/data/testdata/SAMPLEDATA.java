package common.data.testdata;

import common.data.beans.SampleDataBean;

public enum SAMPLEDATA implements IDataBean {
	DEFAULT("default"),
	ONE("one");
	
	String value;

	SAMPLEDATA(String value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getName() {
		return "sampledata";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<SampleDataBean> getClazz() {
		return SampleDataBean.class;
	}

}
