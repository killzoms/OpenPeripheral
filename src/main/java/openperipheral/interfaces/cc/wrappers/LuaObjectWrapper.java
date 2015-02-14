package openperipheral.interfaces.cc.wrappers;

import java.util.Arrays;

import openmods.Log;
import openperipheral.adapter.AdapterLogicException;
import openperipheral.adapter.IMethodExecutor;
import openperipheral.adapter.composed.IndexedMethodMap;
import openperipheral.api.Constants;
import openperipheral.interfaces.cc.ComputerCraftEnv;
import openperipheral.interfaces.cc.ModuleComputerCraft;

import org.apache.logging.log4j.Level;

import com.google.common.base.Preconditions;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;

public class LuaObjectWrapper {

	private static class WrappedLuaObject implements ILuaObject {
		private final IndexedMethodMap methods;
		private final Object target;

		private WrappedLuaObject(IndexedMethodMap methods, Object target) {
			this.methods = methods;
			this.target = target;
		}

		@Override
		public String[] getMethodNames() {
			return methods.getMethodNames();
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
			IMethodExecutor executor = methods.getMethod(method);
			Preconditions.checkNotNull(executor, "Invalid method index: %d", method);

			try {
				return ComputerCraftEnv.addCommonArgs(executor.startCall(target))
						.setOptionalArg(Constants.ARG_CONTEXT, context)
						.call(arguments);
			} catch (LuaException e) {
				throw e;
			} catch (InterruptedException e) {
				throw e;
			} catch (Throwable t) {
				String methodName = methods.getMethodName(method);
				Log.log(Level.DEBUG, t.getCause(), "Internal error during method %s(%d) execution on object %s, args: %s",
						methodName, method, target.getClass(), Arrays.toString(arguments));

				throw new LuaException(AdapterLogicException.getMessageForThrowable(t));
			}
		}
	}

	public static ILuaObject wrap(Object target) {
		Preconditions.checkNotNull(target, "Can't wrap null");
		IndexedMethodMap methods = ModuleComputerCraft.OBJECT_METHODS_FACTORY.getAdaptedClass(target.getClass());
		return methods.isEmpty()? null : new WrappedLuaObject(methods, target);
	}
}
