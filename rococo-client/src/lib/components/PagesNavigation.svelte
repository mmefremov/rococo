<script lang="ts">
    export let isBigScreen: boolean;
</script>
<nav class={`list-nav ${isBigScreen ? "hidden md:block" : "md:hidden w-full"}`}>
    <ul class={`flex items-baseline ${!isBigScreen ? "justify-around" : ""}`}>
        <li>
            <a data-testid="painting-page-link" href="/painting">
                Картины
            </a>
        </li>
        <li>
            <a data-testid="artist-page-link" href="/artist">
                Художники
            </a>
        </li>
        <li>
            <a data-testid="museum-page-link" href="/museum">
                Музеи
            </a>
        </li>
    </ul>
</nav>
