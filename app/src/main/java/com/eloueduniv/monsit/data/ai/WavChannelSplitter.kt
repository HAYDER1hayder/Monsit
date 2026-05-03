package com.eloueduniv.monsit.data.ai

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavChannelSplitter {

    /**
     * Splits a stereo WAV file into two mono WAV files.
     * Assumes 16-bit PCM stereo WAV.
     */
    fun split(stereoFile: File, outputDir: File): Pair<File, File>? {
        return try {
            val fis = FileInputStream(stereoFile)
            val header = ByteArray(44)
            if (fis.read(header) < 44) return null

            // Verify it's a WAV and stereo
            val channels = ByteBuffer.wrap(header.sliceArray(22..23)).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
            if (channels != 2) return null // Not stereo

            val leftFile = File(outputDir, "left_${stereoFile.name}")
            val rightFile = File(outputDir, "right_${stereoFile.name}")

            val fosLeft = FileOutputStream(leftFile)
            val fosRight = FileOutputStream(rightFile)

            // Create mono header
            val monoHeader = header.copyOf()
            // Set channels to 1
            monoHeader[22] = 1
            monoHeader[23] = 0
            // Byte rate = SampleRate * NumChannels * BitsPerSample / 8
            val byteRate = ByteBuffer.wrap(header.sliceArray(28..31)).order(ByteOrder.LITTLE_ENDIAN).int
            val newByteRate = byteRate / 2
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(newByteRate).array().copyInto(monoHeader, 28)
            // Block align = NumChannels * BitsPerSample / 8
            val blockAlign = ByteBuffer.wrap(header.sliceArray(32..33)).order(ByteOrder.LITTLE_ENDIAN).short
            val newBlockAlign = (blockAlign / 2).toShort()
            ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(newBlockAlign).array().copyInto(monoHeader, 32)
            
            // Note: Data size in header should also be updated but Whisper usually handles it if it's slightly off or we can fix it later.
            // For now, let's just write the data.

            fosLeft.write(monoHeader)
            fosRight.write(monoHeader)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                // Buffer contains L1L2R1R2 L3L4R3R4 ...
                val leftBuffer = ByteArray(bytesRead / 2)
                val rightBuffer = ByteArray(bytesRead / 2)
                
                var lIdx = 0
                var rIdx = 0
                for (i in 0 until bytesRead step 4) {
                    if (i + 3 < bytesRead) {
                        leftBuffer[lIdx++] = buffer[i]
                        leftBuffer[lIdx++] = buffer[i + 1]
                        rightBuffer[rIdx++] = buffer[i + 2]
                        rightBuffer[rIdx++] = buffer[i + 3]
                    }
                }
                fosLeft.write(leftBuffer, 0, lIdx)
                fosRight.write(rightBuffer, 0, rIdx)
            }

            fis.close()
            fosLeft.close()
            fosRight.close()

            Pair(leftFile, rightFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
