#include <jni.h>
#include <string>
//#include <draco_decoder.cc>
//#include <decode.h>
#include <src/draco/io/parser_utils.h>
#include <src/draco/io/obj_encoder.h>
#include <cinttypes>
#include <src/draco/io/ply_encoder.h>
#include <src/draco/compression/config/compression_shared.h>
#include <src/draco/mesh/mesh.h>
#include <src/draco/compression/decode.h>
#include <ios>
#include <fstream>
#include <vector>

#include <src/draco/core/decoder_buffer.h>
#include <src/draco/core/cycle_timer.h>
#include <src/draco/point_cloud/point_cloud.h>
#include <src/draco/core/status.h>
#include <src/draco/core/statusor.h>

//#include <cinttypes>
//#include <fstream>
//#include "src/draco/compression/decode.h"
//#include "src/draco/core/cycle_timer.h"
//#include "src/draco/io/obj_encoder.h"
//#include "src/draco/io/parser_utils.h"
//#include "src/draco/io/ply_encoder.h"

extern "C"
JNIEXPORT jint JNICALL
Java_sadappp_myapplication_model3D_view_DemoActivity_decoder(JNIEnv *env, jobject instance,
                                                             jstring dracoFile_, jstring objFile_) {
    const char *dracoFile = env->GetStringUTFChars(dracoFile_, 0);
    const char *objFile = env->GetStringUTFChars(objFile_, 0);

    // TODO

    env->ReleaseStringUTFChars(dracoFile_, dracoFile);
    env->ReleaseStringUTFChars(objFile_, objFile);

//    struct Options {
//        Options();
//
//        std::string input;
//        std::string output;
//    };
//
//    Options::Options() {}

//    int ReturnError(const draco::Status &status) {
//        printf("Failed to decode the input file %s\n", status.error_msg());
//        return -1;
//    }

//    int decodeMachine(char inputArg, char outputArg) {//int main(int argc, char **argv) {
       // Options options;
//  const int argc_check = argc - 1;
//
//  for (int i = 1; i < argc; ++i) {
//    if (!strcmp("-h", argv[i]) || !strcmp("-?", argv[i])) {
//      Usage();
//      return 0;
//    } else if (!strcmp("-i", argv[i]) && i < argc_check) {
//      options.input = argv[++i];
//    } else if (!strcmp("-o", argv[i]) && i < argc_check) {
//      options.output = argv[++i];
//    }
//  }
//  if (argc < 3 || options.input.empty()) {
//    Usage();
//    return -1;
//  }
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
            <draco::Mesh> in_mesh = std::move(statusor).value();
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