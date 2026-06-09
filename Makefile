android-codegen:
	RUST_LOG=info cargo run \
		--package taprux-core \
		--bin codegen \
		--features codegen \
		-- --language kotlin --output-dir taprux-android/generated

.PHONY: android-codegen