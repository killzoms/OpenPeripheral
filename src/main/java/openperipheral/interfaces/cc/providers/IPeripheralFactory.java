package openperipheral.interfaces.cc.providers;

import dan200.computercraft.api.peripheral.IPeripheral;

public interface IPeripheralFactory<T> {
	public IPeripheral getPeripheral(T obj, int side);
}
