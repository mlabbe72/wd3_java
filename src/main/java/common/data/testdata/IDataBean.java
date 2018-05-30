package common.data.testdata;

public interface IDataBean {
	public String getValue();
	
	public String getName();
	
	public <T> Class<T> getClazz();
	
}
