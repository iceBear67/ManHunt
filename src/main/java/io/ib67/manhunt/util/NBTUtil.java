package io.ib67.manhunt.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class NBTUtil {
    private static MethodHandle asNMSCopy;
    private static MethodHandle asBukkitCopy;
    private static MethodHandle hasTag;
    private static MethodHandle getTag;
    private static MethodHandle conNBTTagCompound;
    private static MethodHandle get;
    private static MethodHandle set;
    private static MethodHandle setTag;
    private static MethodHandle asByte;
    private static MethodHandle asString;
    private static MethodHandle asInt;
    private static MethodHandle conNBTTagByte;
    private static MethodHandle conNBTTagString;
    private static MethodHandle conNBTTagInt;
    public static String serverVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(
            ",")[3];

    static {
        try {
            Class<?> NMSItemStack = Class.forName("net.minecraft.server." +
                    serverVersion +
                    ".ItemStack");
            Class<?> NBTBase = Class.forName("net.minecraft.server." + serverVersion + ".NBTBase");
            Class<?> NBTTagCompound = Class.forName("net.minecraft.server." +
                    serverVersion +
                    ".NBTTagCompound");
            Class<?> CraftItemStack = Class.forName("org.bukkit.craftbukkit." +
                    serverVersion +
                    ".inventory.CraftItemStack");
            Class<?> NBTTagByte = Class.forName("net.minecraft.server." +
                    serverVersion +
                    ".NBTTagByte");
            Class<?> NBTTagString = Class.forName("net.minecraft.server." +
                    serverVersion +
                    ".NBTTagString");
            Class<?> NBTTagInt = Class.forName("net.minecraft.server." + serverVersion + ".NBTTagInt");

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            asNMSCopy = lookup.findStatic(CraftItemStack,
                    "asNMSCopy",
                    MethodType.methodType(NMSItemStack, ItemStack.class));
            asBukkitCopy = lookup.findStatic(CraftItemStack,
                    "asBukkitCopy",
                    MethodType.methodType(ItemStack.class, NMSItemStack));
            hasTag = lookup.findVirtual(NMSItemStack, "hasTag", MethodType.methodType(boolean.class));
            getTag = lookup.findVirtual(NMSItemStack, "getTag", MethodType.methodType(NBTTagCompound));
            conNBTTagCompound = lookup.findConstructor(NBTTagCompound, MethodType.methodType(void.class));
            get = lookup.findVirtual(NBTTagCompound, "get", MethodType.methodType(NBTBase, String.class));
            try {
                set = lookup.findVirtual(NBTTagCompound,
                        "set",
                        MethodType.methodType(void.class, String.class, NBTBase));
            } catch (NoSuchMethodException | NoSuchMethodError e) {
                set = lookup.findVirtual(NBTTagCompound, "set", MethodType.methodType(NBTBase, String.class, NBTBase));
            }
            setTag = lookup.findVirtual(NMSItemStack, "setTag", MethodType.methodType(void.class, NBTTagCompound));
            asByte = lookup.findVirtual(NBTTagByte, "asByte", MethodType.methodType(byte.class));
            asString = lookup.findVirtual(NBTTagString, "asString", MethodType.methodType(String.class));
            asInt = lookup.findVirtual(NBTTagInt, "asInt", MethodType.methodType(int.class));
            try {
                conNBTTagByte = lookup.findConstructor(NBTTagByte, MethodType.methodType(void.class, byte.class));
            } catch (IllegalAccessException | IllegalAccessError e) {
                conNBTTagByte = lookup.findStatic(NBTTagByte, "a", MethodType.methodType(NBTTagByte, byte.class));
            }
            try {
                conNBTTagString = lookup.findConstructor(NBTTagString, MethodType.methodType(void.class, String.class));
            } catch (IllegalAccessException | IllegalAccessError e) {
                conNBTTagString = lookup.findStatic(NBTTagString,
                        "a",
                        MethodType.methodType(NBTTagString, String.class));
            }
            try {
                conNBTTagInt = lookup.findConstructor(NBTTagInt, MethodType.methodType(void.class, int.class));
            } catch (IllegalAccessException | IllegalAccessError e) {
                conNBTTagInt = lookup.findStatic(NBTTagInt, "a", MethodType.methodType(NBTTagInt, int.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ManHunt] Plugin shutting down...");
            Bukkit.getServer().broadcastMessage("ManHunt has exception,game suspended.");
        }
    }

    public static NBTValue getTagValue(ItemStack item, String key) {
        Object result = null;
        try {
            Object nis = asNMSCopy.invoke(item);
            Object tag = (((Boolean) hasTag.bindTo(nis).invoke()) ?
                    (getTag.bindTo(nis).invoke()) :
                    newNBTTagCompound());
            result = get.bindTo(tag).invoke(key);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result == null ? null : new NBTValue(result);
    }

    public static ItemStack setTagValue(ItemStack item, String key, NBTValue value) {
        try {
            Object nis = asNMSCopy.invoke(item);
            Object tag = setTagValue((((Boolean) hasTag.bindTo(nis).invoke()) ?
                    (getTag.bindTo(nis).invoke()) :
                    newNBTTagCompound()), key, value);
            setTag.bindTo(nis).invoke(tag);
            return (ItemStack) asBukkitCopy.invoke(nis);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object newNBTTagCompound() {
        try {
            return conNBTTagCompound.invoke();
        } catch (Throwable e) {
            throw new RuntimeException("Exception while getting new instance of NBTTagCompound", e);
        }
    }

    public static Object setTagValue(Object nbttc, String key, NBTValue value) {
        try {
            set.bindTo(nbttc).invoke(key, value.convert());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return nbttc;
    }

    public static class NBTValue {
        private boolean canEdit;
        private Object base;

        public NBTValue() {
            canEdit = true;
        }

        public NBTValue(Object base) {
            canEdit = false;
            this.base = base;
        }

        public boolean asBoolean() {
            if (canEdit)
                return (Boolean) base;
            else {
                boolean result = false;
                try {
                    result = ((Byte) asByte.bindTo(base).invoke()) != 0;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return result;
            }
        }

        public String asString() {
            if (canEdit)
                return (String) base;
            else {
                String result = "";
                try {
                    result = (String) asString.bindTo(base).invoke();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return result;
            }
        }

        public int asInt() {
            if (canEdit)
                return (Integer) base;
            else {
                int result = 0;
                try {
                    result = (int) asInt.bindTo(base).invoke();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return result;
            }
        }

        public NBTValue set(Object obj) {
            if (canEdit)
                base = obj;
            else
                throw new IllegalStateException("This object is read-only!");
            return this;
        }

        protected Object convert() {
            if (canEdit) {
                if (base instanceof Boolean) {
                    try {
                        return conNBTTagByte.invoke((byte) (((Boolean) base) ? 1 : 0));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    return null;
                } else if (base instanceof String) {
                    try {
                        return conNBTTagString.invoke((String) base);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    return null;
                } else if (base instanceof Integer) {
                    try {
                        return conNBTTagInt.invoke((Integer) base);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    return null;
                } else
                    throw new IllegalStateException("Unknown value type!");
            } else
                return base;
        }
    }
}
