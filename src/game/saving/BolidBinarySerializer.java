package saving;

import domain.*;

import java.io.*;
import java.util.Map;

public class BolidBinarySerializer {

    private static final byte MAGIC_1  = (byte) 0xB0;
    private static final byte MAGIC_2  = (byte) 0x11;
    private static final byte VERSION  = 1;

    // сохраняет болид в бинарный файл
    public void save(Bolid bolid, File file) throws IOException {
        file.getParentFile().mkdirs();
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            writeHeader(out);
            writeBolid(out, bolid);
        }
    }

    // загружает болид из бинарного файла
    public Bolid load(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            readAndValidateHeader(in);
            return readBolid(in);
        }
    }

    // write

    private void writeHeader(DataOutputStream out) throws IOException {
        out.writeByte(MAGIC_1);
        out.writeByte(MAGIC_2);
        out.writeByte(VERSION);
    }

    private void writeBolid(DataOutputStream out, Bolid bolid) throws IOException {
        out.writeUTF(bolid.getName());

        // основные компоненты
        Map<ComponentType, Component> comps = bolid.getComponents();
        out.writeInt(comps.size());
        for (Component c : comps.values()) {
            writeComponent(out, c);
        }

        // дополнительные компоненты
        out.writeInt(bolid.getExtras().size());
        for (Component c : bolid.getExtras()) {
            writeComponent(out, c);
        }

        // оружие
        Map<WeaponType, Weapon> weapons = bolid.getWeapons();
        out.writeInt(weapons.size());
        for (Weapon w : weapons.values()) {
            writeWeapon(out, w);
        }
    }

    private void writeComponent(DataOutputStream out, Component c) throws IOException {
        out.writeUTF(c.getName());
        out.writeUTF(c.getType().name());
        out.writeInt(c.getPrice());
        out.writeInt(c.getPerformanceValue());
        out.writeInt(c.getWear());
        out.writeInt(c.getLevel());
    }

    private void writeWeapon(DataOutputStream out, Weapon w) throws IOException {
        out.writeUTF(w.getName());
        out.writeUTF(w.getType().name());
        out.writeInt(w.getPrice());
        out.writeInt(w.getDamage());
        out.writeInt(w.getLevel());
    }

    // read

    private void readAndValidateHeader(DataInputStream in) throws IOException {
        byte m1 = in.readByte();
        byte m2 = in.readByte();
        if (m1 != MAGIC_1 || m2 != MAGIC_2) {
            throw new IOException("Неверный формат файла: ожидалось 0xB011, получено 0x"
                    + String.format("%02X%02X", m1 & 0xFF, m2 & 0xFF));
        }
        byte version = in.readByte();
        if (version != VERSION) {
            throw new IOException("Неподдерживаемая версия формата болида: " + version);
        }
    }

    private Bolid readBolid(DataInputStream in) throws IOException {
        Bolid bolid = new Bolid(in.readUTF());

        int compCount = in.readInt();
        for (int i = 0; i < compCount; i++) {
            bolid.installComponent(readComponent(in));
        }

        int extraCount = in.readInt();
        for (int i = 0; i < extraCount; i++) {
            bolid.addExtra(readComponent(in));
        }

        int weaponCount = in.readInt();
        for (int i = 0; i < weaponCount; i++) {
            bolid.installWeapon(readWeapon(in));
        }

        return bolid;
    }

    private Component readComponent(DataInputStream in) throws IOException {
        String name  = in.readUTF();
        ComponentType type = ComponentType.valueOf(in.readUTF());
        int price = in.readInt();
        int perf = in.readInt();
        int wear = in.readInt();
        int level = in.readInt();

        Component c = new Component(name, type, price, perf, level);
        c.setWear(wear);
        return c;
    }

    private Weapon readWeapon(DataInputStream in) throws IOException {
        String name = in.readUTF();
        WeaponType type = WeaponType.valueOf(in.readUTF());
        int price = in.readInt();
        int damage = in.readInt();
        int level = in.readInt();

        return new Weapon(name, type, price, damage, level);
    }
}
