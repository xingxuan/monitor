package cn.thextrader.device.tools.vm.core.datasource;

public final class DataSourceContainer extends DataSourceProvider {
	private final DataSource owner;
	
	DataSourceContainer(DataSource owner) {
        this.owner = owner;
    }

	public void addDataSource(DataSource added) {
        registerDataSource(added);
    }
	
	public void removeDataSource(DataSource removed) {
        unregisterDataSource(removed);
    }
}
