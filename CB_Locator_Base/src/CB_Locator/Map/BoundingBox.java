package CB_Locator.Map;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BoundingBox {
    public final int SizeOf = 28;
    public int MinX;
    public int MaxX;
    public int MinY;
    public int MaxY;
    public int Zoom;
    public long OffsetToIndex;
    public int Stride;

    public BoundingBox(int zoom, int minX, int maxX, int minY, int maxY, long offset) {
        MinX = minX;
        MaxX = maxX;
        MinY = minY;
        MaxY = maxY;
        Zoom = zoom;
        OffsetToIndex = offset;
        Stride = MaxX - MinX + 1;
    }

    public BoundingBox(DataInput reader) {
        try {
            Zoom = Integer.reverseBytes(reader.readInt());
            MinX = Integer.reverseBytes(reader.readInt());
            MaxX = Integer.reverseBytes(reader.readInt());
            MinY = Integer.reverseBytes(reader.readInt());
            MaxY = Integer.reverseBytes(reader.readInt());
            OffsetToIndex = Long.reverseBytes(reader.readLong());
        } catch (IOException e) {

            e.printStackTrace();
        }
        Stride = MaxX - MinX + 1;
    }

    public static BoundingBox ReadInstance(DataInput reader) {

        try {
            return new BoundingBox(Integer.reverseBytes(reader.readInt()), Integer.reverseBytes(reader.readInt()), Integer.reverseBytes(reader.readInt()), Integer.reverseBytes(reader.readInt()), Integer.reverseBytes(reader.readInt()),
                    Long.reverseBytes(reader.readLong()));
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    public int NumTilesTotal() {
        return (MaxX - MinX + 1) * (MaxY - MinY + 1);
    }

    public boolean Contains(int x, int y) {
        return x <= MaxX && x >= MinX && y <= MaxY && y >= MinY;
    }

    public void Write(DataOutput writer) {
        try {
            writer.writeInt(Integer.reverseBytes(Zoom));
            writer.writeInt(Integer.reverseBytes(MinX));
            writer.writeInt(Integer.reverseBytes(MaxX));
            writer.writeInt(Integer.reverseBytes(MinY));
            writer.writeInt(Integer.reverseBytes(MaxY));
            writer.writeLong(Long.reverseBytes(OffsetToIndex));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
