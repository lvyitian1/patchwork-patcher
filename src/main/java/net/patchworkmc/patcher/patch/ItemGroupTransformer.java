package net.patchworkmc.patcher.patch;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.patchworkmc.patcher.ForgeModJar;
import net.patchworkmc.patcher.Patchwork;
import net.patchworkmc.patcher.transformer.api.ClassPostTransformer;
import net.patchworkmc.patcher.transformer.api.Transformer;
import net.patchworkmc.patcher.util.MinecraftVersion;

public class ItemGroupTransformer extends Transformer {
	// The intermediary name for ItemGroup
	private static final String ITEM_GROUP = "net/minecraft/class_1761";

	// Patchwork's replacement item group classs
	private static final String PATCHWORK_ITEM_GROUP = "net/patchworkmc/api/redirects/itemgroup/PatchworkItemGroup";

	private static final String VANILLA_CREATE_ICON = "method_7750";
	private static final String VANILLA_CREATE_ICON_DESC = "()Lnet/minecraft/class_1799;";

	private static final String PATCHWORK_CREATE_ICON = "patchwork$createIcon";
	private boolean applies = false;

	public ItemGroupTransformer(MinecraftVersion version, ForgeModJar jar, ClassVisitor parent, ClassPostTransformer widenings) {
		super(version, jar, parent, widenings);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (superName.equals(ITEM_GROUP)) {
			superName = PATCHWORK_ITEM_GROUP;
			applies = true;
		}

		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if (applies && name.equals("<init>")) {
			return new MethodTransformer(super.visitMethod(access, name, descriptor, signature, exceptions));
		} else if (name.equals(VANILLA_CREATE_ICON) && descriptor.equals(VANILLA_CREATE_ICON_DESC)) {
			return super.visitMethod(access, PATCHWORK_CREATE_ICON, descriptor, signature, exceptions);
		} else {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}

	private static class MethodTransformer extends MethodVisitor {
		private MethodTransformer(MethodVisitor parent) {
			super(Opcodes.ASM9, parent);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			if (opcode != Opcodes.INVOKESPECIAL) {
				super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
			}

			if (!owner.equals(ITEM_GROUP) || !name.equals("<init>")) {
				return;
			}

			if (!descriptor.equals("(Ljava/lang/String;)V") && !descriptor.equals("(ILjava/lang/String;)V")) {
				Patchwork.LOGGER.error("Unexpected descriptor for super() in ItemGroup: " + descriptor);
			}

			super.visitMethodInsn(Opcodes.INVOKESPECIAL, PATCHWORK_ITEM_GROUP, name, descriptor, isInterface);
		}
	}
}
