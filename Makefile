android-codegen:
	RUST_LOG=info cargo run \
		--package taprux-core \
		--bin codegen \
		--features codegen \
		-- --language kotlin --output-dir taprux-android/generated

pack-android:
	cd taprux-core && boltffi pack android --release

.PHONY: android-codegen