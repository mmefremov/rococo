<script lang="ts">

    export let submitButtonText = "Добавить";
    export let closeButtonText = "Закрыть";
    export let onClose = () => {};

</script>

<div class="text-right">
    <button data-testid="close-button" class="btn variant-ringed" type="button" on:click={onClose}>{closeButtonText}</button>
    <button data-testid="submit-button" class="btn variant-filled-primary" type="submit">{submitButtonText}</button>
</div>
