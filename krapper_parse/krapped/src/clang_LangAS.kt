package clang

// BEGIN KRAPPER GEN for enum LangAS
enum class LangAS(val value: UInt) {
    Default(0u), opencl_global(1u), opencl_local(2u), opencl_constant(3u), opencl_private(4u), opencl_generic(5u), opencl_global_device(6u), opencl_global_host(7u), cuda_device(8u), cuda_constant(9u), cuda_shared(10u), sycl_global(11u), sycl_global_device(12u), sycl_global_host(13u), sycl_local(14u), sycl_private(15u), ptr32_sptr(16u), ptr32_uptr(17u), ptr64(18u), hlsl_groupshared(19u), hlsl_constant(20u), hlsl_private(21u), hlsl_device(22u), hlsl_input(23u), hlsl_push_constant(24u), wasm_funcref(25u), FirstTargetAddressSpace(26u);

    companion object {
        fun fromValue(v: UInt): LangAS = entries.firstOrNull { it.value == v } ?: entries.first()
    }
}
// END KRAPPER GEN for enum LangAS
