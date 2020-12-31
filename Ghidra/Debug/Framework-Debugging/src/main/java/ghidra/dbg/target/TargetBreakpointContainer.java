/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.dbg.target;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import ghidra.dbg.DebuggerTargetObjectIface;
import ghidra.dbg.attributes.TargetObjectRef;
import ghidra.dbg.attributes.TypedTargetObjectRef;
import ghidra.dbg.target.TargetBreakpointSpec.TargetBreakpointKind;
import ghidra.dbg.util.CollectionUtils.AbstractEmptySet;
import ghidra.dbg.util.CollectionUtils.AbstractNSet;
import ghidra.program.model.address.*;

@DebuggerTargetObjectIface("BreakpointContainer")
public interface TargetBreakpointContainer<T extends TargetBreakpointContainer<T>>
		extends TypedTargetObject<T> {
	enum Private {
		;
		private abstract class Cls implements TargetBreakpointContainer<Cls> {
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	Class<Private.Cls> tclass = (Class) TargetBreakpointContainer.class;

	String SUPPORTED_BREAK_KINDS_ATTRIBUTE_NAME = PREFIX_INVISIBLE + "supported_breakpoint_kinds";

	public interface TargetBreakpointKindSet extends Set<TargetBreakpointKind> {
		public static class EmptyTargetBreakpointKindSet
				extends AbstractEmptySet<TargetBreakpointKind> implements TargetBreakpointKindSet {
			// Nothing
		}

		public static class ImmutableTargetBreakpointKindSet
				extends AbstractNSet<TargetBreakpointKind>
				implements TargetBreakpointKindSet {

			public ImmutableTargetBreakpointKindSet(TargetBreakpointKind... kinds) {
				super(kinds);
			}

			public ImmutableTargetBreakpointKindSet(Set<TargetBreakpointKind> set) {
				super(set);
			}
		}

		TargetBreakpointKindSet EMPTY = new EmptyTargetBreakpointKindSet();

		public static TargetBreakpointKindSet of() {
			return EMPTY;
		}

		public static TargetBreakpointKindSet of(TargetBreakpointKind... kinds) {
			return new ImmutableTargetBreakpointKindSet(kinds);
		}

		public static TargetBreakpointKindSet copyOf(Set<TargetBreakpointKind> set) {
			return new ImmutableTargetBreakpointKindSet(set);
		}
	}

	public default TargetBreakpointKindSet getSupportedBreakpointKinds() {
		return getTypedAttributeNowByName(SUPPORTED_BREAK_KINDS_ATTRIBUTE_NAME,
			TargetBreakpointKindSet.class, TargetBreakpointKindSet.of());
	}

	public CompletableFuture<Void> placeBreakpoint(String expression,
			Set<TargetBreakpointKind> kinds);

	public CompletableFuture<Void> placeBreakpoint(AddressRange range,
			Set<TargetBreakpointKind> kinds);

	public default CompletableFuture<Void> placeBreakpoint(Address address,
			Set<TargetBreakpointKind> kinds) {
		return placeBreakpoint(new AddressRangeImpl(address, address), kinds);
	}

	public interface TargetBreakpointListener extends TargetObjectListener {
		/**
		 * A breakpoint trapped execution
		 * 
		 * <p>
		 * The program counter can be obtained in a few ways. The most reliable is to get the
		 * address of the effective breakpoint. If available, the frame will also contain the
		 * program counter. Finally, the trapped object or one of its relatives may offer the
		 * program counter.
		 * 
		 * @param container the container whose breakpoint trapped execution
		 * @param trapped the object whose execution was trapped
		 * @param frame the innermost stack frame, if available, of the trapped object
		 * @param spec the breakpoint specification
		 * @param breakpoint the breakpoint location that actually trapped execution
		 */
		default void breakpointHit(TargetBreakpointContainer<?> container, TargetObjectRef trapped,
				TypedTargetObjectRef<? extends TargetStackFrame<?>> frame,
				TypedTargetObjectRef<? extends TargetBreakpointSpec<?>> spec,
				TypedTargetObjectRef<? extends TargetBreakpointLocation<?>> breakpoint) {
		}
	}
}