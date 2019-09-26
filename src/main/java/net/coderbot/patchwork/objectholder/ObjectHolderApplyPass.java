package net.coderbot.patchwork.objectholder;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ObjectHolderApplyPass extends ClassVisitor {
	private static int EXPECTED_ACCESS = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;

	private boolean global;
	private Predicate<String> transformed;
	private HashMap<String, String> fields;

	public ObjectHolderApplyPass(ClassVisitor parent, boolean global, Predicate<String> transformed) {
		super(Opcodes.ASM7, parent);

		this.global = global;
		this.transformed = transformed;
		this.fields = new HashMap<>();
	}

	public Map<String, String> getHolderFields() {
		return fields;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		boolean match = transformed.test(name);

		if(match && access != EXPECTED_ACCESS) {
			throw new IllegalArgumentException("Field " + name + " marked with an @ObjectHolder annotation did not have the expected access of public static final");
		}

		if(match || (global && access == EXPECTED_ACCESS)) {
			access = access & (~Opcodes.ACC_FINAL);

			fields.put(name, descriptor);
		}

		return super.visitField(access, name, descriptor, signature, value);
	}
}
