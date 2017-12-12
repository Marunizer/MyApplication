#include <jni.h>
#include <string>
#include <cinttypes>
#include <fstream>

#include "compression/decode.h"
#include "core/cycle_timer.h"
#include "io/obj_encoder.h"
#include "io/parser_utils.h"
#include "io/ply_encoder.h"

extern "C"
JNIEXPORT jint JNICALL
Java_sadappp_myapplication_model3D_view_DemoActivity_decoder(JNIEnv *env, jobject instance,
                                                             jstring dracoFile_, jstring objFile_) {
    const char *dracoFile = env->GetStringUTFChars(dracoFile_, 0);
    const char *objFile = env->GetStringUTFChars(objFile_, 0);

    env->ReleaseStringUTFChars(dracoFile_, dracoFile);
    env->ReleaseStringUTFChars(objFile_, objFile);

    std::string dinput;
    std::string doutput;
    dinput = dracoFile;
    doutput = objFile;


    std::ifstream input_file(dinput, std::ios::binary);
    if (!input_file) {
        printf("Failed opening the input file.\n");
        return -1;
    }

    // Read the file stream into a buffer.
    std::streampos file_size = 0;
    input_file.seekg(0, std::ios::end);
    file_size = input_file.tellg() - file_size;
    input_file.seekg(0, std::ios::beg);
    std::vector<char> data(file_size);
    input_file.read(data.data(), file_size);

    if (data.empty()) {
        printf("Empty input file.\n");
        return -1;
    }

    // Create a draco decoding buffer. Note that no data is copied in this step.
    draco::DecoderBuffer buffer;
    buffer.Init(data.data(), data.size());

    draco::CycleTimer timer;
    // Decode the input data into a geometry.
    std::unique_ptr<draco::PointCloud> pc;
    draco::Mesh *mesh = nullptr;
    auto type_statusor = draco::Decoder::GetEncodedGeometryType(&buffer);
    if (!type_statusor.ok()) {
        // return ReturnError(type_statusor.status());
    }
    const draco::EncodedGeometryType geom_type = type_statusor.value();
    if (geom_type == draco::TRIANGULAR_MESH) {
        timer.Start();
        draco::Decoder decoder;
        auto statusor = decoder.DecodeMeshFromBuffer(&buffer);
        if (!statusor.ok()) {
            //   return ReturnError(statusor.status());
        }
        std::unique_ptr<draco::Mesh> in_mesh = std::move(statusor).value();
        timer.Stop();
        if (in_mesh) {
            mesh = in_mesh.get();
            pc = std::move(in_mesh);
        }
    }

    if (pc == nullptr) {
        printf("Failed to decode the input file.\n");
        return -1;
    }

    if (doutput.empty()) {
        // Save the output model into a ply file.
        doutput = dinput + ".ply";
    }

    // Save the decoded geometry into a file.
    // TODO(ostava): Currently only .ply and .obj are supported.
    const std::string extension = draco::parser::ToLower(
            doutput.size() >= 4
            ? doutput.substr(doutput.size() - 4)
            : doutput);

    if (extension == ".obj") {
        draco::ObjEncoder obj_encoder;
        if (mesh) {
            if (!obj_encoder.EncodeToFile(*mesh, doutput)) {
                printf("Failed to store the decoded mesh as OBJ.\n");
                return -1;
            }
        } else {
            if (!obj_encoder.EncodeToFile(*pc.get(), doutput)) {
                printf("Failed to store the decoded point cloud as OBJ.\n");
                return -1;
            }
        }
    } else if (extension == ".ply") {
        draco::PlyEncoder ply_encoder;
        if (mesh) {
            if (!ply_encoder.EncodeToFile(*mesh, doutput)) {
                printf("Failed to store the decoded mesh as PLY.\n");
                return -1;
            }
        } else {
            if (!ply_encoder.EncodeToFile(*pc.get(), doutput)) {
                printf("Failed to store the decoded point cloud as PLY.\n");
                return -1;
            }
        }
    } else {
        printf("Invalid extension of the output file. Use either .ply or .obj\n");
        return -1;
    }
    printf("Decoded geometry saved to %s (%" PRId64 " ms to decode)\n",
           doutput.c_str(), timer.GetInMs());
    return 0;
//    }
//
//    decodeMachine(dracoFile,objFile);

//    const char *returnValue = objFile;
//    return env->NewStringUTF(returnValue);
}